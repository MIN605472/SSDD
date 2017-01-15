defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    #@enforce_keys [:num_vista]
    defstruct  [:num_vista, :primario, :copia]

    @periodo_latido 50

    @latidos_fallidos 4



    @doc """
        Poner en marcha el servidor para gestión de vistas
    """
    @spec start(String.t, String.t) :: atom
    def start(host, nombre_nodo) do
        nodo = NodoRemoto.start(host, nombre_nodo, __ENV__.file, __MODULE__)
        Node.spawn_link(nodo, __MODULE__, :init_sv, [])

        nodo
    end


    #------------------- Funciones privadas
    # Estas 2 primeras deben ser defs para llamadas tipo (MODULE, funcion,[])
    def init_sv() do
        Process.register(self(), :servidor_gv)

        spawn(__MODULE__, :init_monitor, [self()]) # otro proceso concurrente

        vistaTentativa = %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
        Agent.start_link(fn -> vistaTentativa end, name: :vTentativa)
        Agent.start_link(fn -> vistaTentativa end, name: :vValida)
        Agent.start_link(fn -> Map.new() end, name: :lista_latidos)

        bucle_recepcion()
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido * @latidos_fallidos)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion() do
        receive do
            {:latido, nodo_origen, n_vista} ->
                if not sistema_caido?() do
                    aux = fn map -> Map.update(map, nodo_origen, 1, fn x -> x + 1 end) end
                    Agent.update(:lista_latidos, aux)
                    nueva_vista = procesa_latido(nodo_origen, n_vista)
                    send({:servidor_sa, nodo_origen}, {:vista_tentativa, nueva_vista, false})
                end

            {:obten_vista, pid} ->
                vistaValida = Agent.get(:vValida, fn vista -> vista end)
                send(pid, {:vista_valida, vistaValida, true})

            :procesa_situacion_servidores ->
                procesar_situacion_servidores()

        end
        bucle_recepcion()
    end

    defp copia_caido?() do
        copia = Agent.get(:vTentativa, fn vista -> vista.copia end)
        latidos = Agent.get(:lista_latidos, fn x -> x end)
        num = Map.get(latidos, copia)
        copia != :undefined and num == nil
    end
    
    defp primario_caido?() do
        primario = Agent.get(:vTentativa, fn vista -> vista.primario end)
        latidos = Agent.get(:lista_latidos, fn x -> x end)
        num = Map.get(latidos, primario)
        primario != :undefined and num == nil
    end

    defp actualizar_tentativa_si_copia_caido(nodo) do
        if copia_caido?() do
            aux = fn vista ->
                %{vista  | 
                    copia: nodo,
                    num_vista: vista.num_vista + 1
                }
            end
            Agent.update(:vTentativa, aux)
        end
    end

    defp actualizar_tentativa_si_primario_caido(nodo) do
        if primario_caido?() do
            aux = fn vista ->
                %{vista |
                    primario: vista.copia,
                    copia: nodo,
                    num_vista: vista.num_vista + 1
                }
            end
            Agent.update(:vTentativa, aux)
        end
    end


    defp actualizar_tentativa_si_no_hay_primario_o_copia(nodo) do
        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        cond do
            vistaTentativa.primario == :undefined and nodo != vistaTentativa.copia ->
                aux_vista = fn vista -> 
                    %{vista |
                        primario: nodo,
                        num_vista: vista.num_vista + 1
                    }
                end
                Agent.update(:vTentativa, aux_vista)

            vistaTentativa.copia == :undefined and nodo != vistaTentativa.primario->
                aux_vista = fn vista -> 
                    %{vista |
                        copia: nodo,
                        num_vista: vista.num_vista + 1
                    }
                end
                Agent.update(:vTentativa, aux_vista)

            true ->
                :nada
        end
    end

    defp actualizar_tentativa_si_no_hay_copia(nodo) do
        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        if vistaTentativa.copia == :undefined and nodo != vistaTentativa.primario do
            aux_vista = fn vista -> 
                %{vista |
                    copia: nodo,
                    num_vista: vista.num_vista + 1
                }
            end
            Agent.update(:vTentativa, aux_vista)
        end
    end

    defp procesar_situacion_servidores() do
        vistaValida = Agent.get(:vValida, fn vista -> vista end)
        cond do
            # Sistema caido
            sistema_caido?() ->
                IO.inspect "SISTEMA CAIDO!"
            # Funcionamiento inicial
            vistaValida.num_vista == 0 ->
                actualizar_tentativa_si_copia_caido(:undefined)
                actualizar_tentativa_si_primario_caido(:undefined)
            # Funcionamiento normal
            vistaValida.num_vista != 0 ->                
                actualizar_tentativa_si_copia_caido(:undefined)
                actualizar_tentativa_si_primario_caido(:undefined)
        end
        Agent.update(:lista_latidos, fn _ -> Map.new end)
    end

    # Comprueba si se ha caido el sistema. El sistema se cae si al tener una vista valida
    # el primario cae cuando la vista tentativa es diferente a la vista valida
    defp sistema_caido?() do
        primario_tentativa = Agent.get(:vTentativa, fn vista -> vista.primario end)
        valida = Agent.get(:vValida, fn vista -> vista end)
        valida.num_vista != 0 and primario_tentativa != valida.primario and 
            primario_tentativa != valida.copia
    end

    defp actualizar_valida_si_confirma(nodo, num_vista) do
        tentativa = Agent.get(:vTentativa, fn v -> v end)
        if nodo == tentativa.primario and tentativa.num_vista == num_vista do
            Agent.update(:vValida, fn _ -> tentativa end)
        end
    end

    defp actualizar_tentativa_si_caen_rapidamente(nodo) do
        tentativa = Agent.get(:vTentativa, fn vista -> vista end)
        primario_caido = tentativa.primario != :undefined and nodo == tentativa.primario
        copia_caido = tentativa.copia != :undefined and nodo == tentativa.copia
        if primario_caido or copia_caido do
            aux = fn vista ->
                %{vista |
                    num_vista: vista.num_vista + 1
                }   
            end
            Agent.update(:vTentativa, aux)
        end
    end

    defp procesa_latido(nodo, 0) do
        vista_valida = Agent.get(:vValida, fn vista -> vista end)
        cond do
            sistema_caido?() ->
                :nada
            vista_valida.num_vista == 0 ->
                actualizar_tentativa_si_caen_rapidamente(nodo)
                actualizar_tentativa_si_no_hay_primario_o_copia(nodo)
            vista_valida.num_vista != 0 ->
                actualizar_tentativa_si_caen_rapidamente(nodo)
                actualizar_tentativa_si_no_hay_copia(nodo)
        end
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    defp procesa_latido(_, -1) do
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    defp procesa_latido(nodo, num_vista) do
        vista_valida = Agent.get(:vValida, fn vista -> vista end)
        cond do
            sistema_caido?() ->
                :nada
            vista_valida.num_vista == 0 ->
                actualizar_valida_si_confirma(nodo, num_vista)
                actualizar_tentativa_si_no_hay_primario_o_copia(nodo)
            vista_valida.num_vista != 0 ->
                actualizar_valida_si_confirma(nodo, num_vista)
                actualizar_tentativa_si_no_hay_copia(nodo)
        end
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    def vista_inicial do
        %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
    end
end
