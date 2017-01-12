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
        Agent.start_link(fn -> Map.new() end, name: :mon_caidos)

        bucle_recepcion()
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion() do
        receive do
            {:latido, nodo_origen, n_vista} ->
                if not sistema_caido?() do
                    nueva_vista = procesa_latido(nodo_origen, n_vista)
                    aux = fn map -> Map.put(map, nodo_origen, n_vista) end
                    Agent.update(:lista_latidos, aux)
                    send({:cliente_gv, nodo_origen}, {:vista_tentativa, nueva_vista, false})
                end

            {:obten_vista, pid} ->
                vistaValida = Agent.get(:vValida, fn vista -> vista end)
                send(pid, {:vista_valida, vistaValida, true})

            :procesa_situacion_servidores ->
                procesar_situacion_servidores()

        end
        bucle_recepcion()
    end

    # Acutaliza la estructura que mantenemos para saber si se ha caido primario o copia
    defp actualizar_mon_caidos() do
        latidos = Agent.get(:lista_latidos, fn l -> l end)
        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        if vistaTentativa.primario != :undefined do
            if Map.get(latidos, vistaTentativa.primario) == nil do
                Agent.update(:mon_caidos, fn map -> Map.update(map, :primario, 3, fn v -> v - 1 end) end) 
            else
                Agent.update(:mon_caidos, fn map -> Map.put(map, :primario, @latidos_fallidos) end)
            end
        end

        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        if vistaTentativa.copia != :undefined do
            if Map.get(latidos, vistaTentativa.copia) == nil do
                Agent.update(:mon_caidos, fn map -> Map.update(map, :copia, 3, fn v -> v - 1 end) end) 
            else
                Agent.update(:mon_caidos, fn map -> Map.put(map, :copia, @latidos_fallidos) end)
            end
        end

    end

    defp copia_caido?() do
        copia = Agent.get(:vTentativa, fn vista -> vista.copia end)
        mon_caidos = Agent.get(:mon_caidos, fn x -> x end)
        num = Map.get(mon_caidos, :copia)
        copia != :undefined and num == 0
    end
    
    defp primario_caido?() do
        primario = Agent.get(:vTentativa, fn vista -> vista.primario end)
        mon_caidos = Agent.get(:mon_caidos, fn x -> x end)
        num = Map.get(mon_caidos, :primario)
        primario != :undefined and num == 0
    end

    defp actualizar_tentativa_si_copia_caido do
        lista = Agent.get(:lista_latidos, fn l -> l end)
        if copia_caido?() do
            aux = fn vista ->
                %{vista  | 
                    copia: elegir_nodo(Map.to_list(lista), vista.primario),
                    num_vista: vista.num_vista + 1
                }
            end
            Agent.update(:vTentativa, aux)
        end
    end

    defp actualizar_tentativa_si_primario_caido do
        lista = Agent.get(:lista_latidos, fn l -> l end)
        if primario_caido?() do
            aux = fn vista ->
                %{vista |
                    primario: vista.copia,
                    copia: elegir_nodo(Map.to_list(lista), vista.copia),
                    num_vista: vista.num_vista + 1
                }
            end
            Agent.update(:vTentativa, aux)
        end
    end


    defp actualizar_tentativa_si_no_hay_primario do
        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        lista = Agent.get(:lista_latidos, fn l -> l end)
        if vistaTentativa.primario == :undefined do
            nuevo_primario = elegir_nodo(Map.to_list(lista), vistaTentativa.copia)
            if nuevo_primario != :undefined do
                aux_vista = fn vista -> 
                    %{vista |
                        primario: nuevo_primario,
                        num_vista: vista.num_vista + 1
                    }
                end
                aux_mon_caidos = fn map ->
                    Map.put(map, :primario, @latidos_fallidos)  
                end
                Agent.update(:mon_caidos, aux_mon_caidos)
                Agent.update(:vTentativa, aux_vista)
            end
        end
    end

    defp actualizar_tentativa_si_no_hay_copia do
        vistaTentativa = Agent.get(:vTentativa, fn vista -> vista end)
        lista = Agent.get(:lista_latidos, fn l -> l end)
        if vistaTentativa.copia == :undefined do
            nuevo_copia = elegir_nodo(Map.to_list(lista), vistaTentativa.primario)
            if nuevo_copia != :undefined do
                aux_vista = fn vista -> 
                    %{vista |
                        copia: nuevo_copia,
                        num_vista: vista.num_vista + 1
                    }
                end

                aux_mon_caidos = fn map ->
                    Map.put(map, :primario, @latidos_fallidos)  
                end

                Agent.update(:mon_caidos, aux_mon_caidos)
                Agent.update(:vTentativa, aux_vista)
            end
        end
    end

    ##SI CAE NODO, PONER EN ESTRUCTURA A :undefined
    defp procesar_situacion_servidores() do
        vistaValida = Agent.get(:vValida, fn vista -> vista end)
        cond do
            # Sistema caido
            sistema_caido?() ->
                IO.inspect "SISTEMA CAIDO"
            # Funcionamiento inicial
            vistaValida.num_vista == 0 ->
                actualizar_mon_caidos()
                actualizar_valida_si_confirma()
                actualizar_tentativa_si_copia_caido()
                actualizar_tentativa_si_primario_caido()
                actualizar_tentativa_si_no_hay_primario()
                actualizar_tentativa_si_no_hay_copia()
            # Funcionamiento normal
            vistaValida.num_vista != 0 ->                
                actualizar_mon_caidos()
                actualizar_valida_si_confirma()
                actualizar_tentativa_si_copia_caido()
                actualizar_tentativa_si_primario_caido()
                actualizar_tentativa_si_no_hay_copia()
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

    defp elegir_nodo([], _) do
        :undefined
    end

    defp elegir_nodo([{nodo, _} | t], valor) do
        if nodo != valor do
            nodo
        else
            elegir_nodo(t, valor)
        end
    end

    defp actualizar_valida_si_confirma() do
        lista = Agent.get(:lista_latidos, fn l -> l end)
        vistaTentativa = Agent.get(:vTentativa, fn v -> v end)
        valor = Map.get(lista, vistaTentativa.primario)
        if valor != nil and valor == vistaTentativa.num_vista do
            Agent.update(:vValida, fn _ -> vistaTentativa end)
        end
    end

    defp procesa_latido(_, _) do
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    def vista_inicial do
        %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
    end
end
