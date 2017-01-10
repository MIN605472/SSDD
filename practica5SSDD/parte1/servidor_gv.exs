defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    #@enforce_keys [:num_vista]
    defstruct  [:num_vista, :primario, :copia, :valida]

    @periodo_latido 50

    @latidos_fallidos 4

    

    @doc """
        Poner en marcha el servidor para gestión de vistas
    """
    @spec start(String.t, String.t) :: atom
    def start(host, nombre_nodo) do
        IO.puts("pre start_sv")
        nodo = NodoRemoto.start(host, nombre_nodo, __ENV__.file, __MODULE__)
        IO.puts("post start_sv")
        Node.spawn_link(nodo, __MODULE__, :init_sv, [])

        nodo
    end


    #------------------- Funciones privadas
    # Estas 2 primeras deben ser defs para llamadas tipo (MODULE, funcion,[])
    def init_sv() do
        Process.register(self(), :servidor_gv)

        spawn(__MODULE__, :init_monitor, [self()]) # otro proceso concurrente

        vistaTentativa = %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined, valida: false}
        Agent.start_link(fn -> vistaTentativa end, name: :vTentativa)
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
                nueva_vista = procesa_latido(nodo_origen, n_vista)
                aux = fn map -> Map.update(map, nodo_origen, 1, fn x -> x + 1 end) end
                Agent.update(:lista_latidos, aux)
                send({:cliente_gv, nodo_origen}, {:vista_tentativa, nueva_vista, true})

            {:obten_vista, pid} ->
                vista = Agent.get(:vTentativa, fn vista -> vista end)
                send(pid, {:vista_valida, vista, vista.valida})

            :procesa_situacion_servidores ->
                procesar_situacion_servidores()

        end
        bucle_recepcion()
    end

    ##SI CAE NODO, PONER EN ESTRUCTURA A :undefined
    defp procesar_situacion_servidores() do
        vista = Agent.get(:vTentativa, fn vista -> vista end)
        lista = Agent.get(:lista_latidos, fn l -> l end)
        if vista.primario != :undefined and not Map.has_key?(lista, vista.primario) do
          aux = fn vista -> 
                    %{vista  | primario: vista.copia, 
                    copia: elegir_nodo(Map.to_list(lista), vista.copia),
                    num_vista: vista.num_vista + 1,
                    valida: false}
                end
          Agent.update(:vTentativa, aux)
        end

        if vista.copia != :undefined and not Map.has_key?(lista, vista.copia) do
            aux = fn vista ->
                      %{vista  | copia: elegir_nodo(Map.to_list(lista), vista.primario),
                      num_vista: vista.num_vista + 1, 
                      valida: false}
                  end
            Agent.update(:vTentativa, aux) 
        end
        Agent.update(:lista_latidos, fn _ -> Map.new end)
    end

    defp elegir_nodo([], _) do
        :undefined
    end

    defp elegir_nodo([{nodo, _} | t], valor) do
      if nodo != valor do
          IO.inspect nodo
          nodo
      else
          elegir_nodo(t, valor)
      end
    end

    defp procesa_latido(nodo, 0) do
        vistaTentativa = Agent.get(:vTentativa, fn(vista) -> vista end)
        ##si recibimos un 0 y no hay primario
        if vistaTentativa.primario == :undefined do
            Agent.update(:vTentativa, fn(vista) -> %{vista |
                num_vista: vista.num_vista + 1, primario: nodo} end)        
        else
            ##si recibimos un 0 y hay primario pero no hay copia
            if vistaTentativa.copia == :undefined do
                Agent.update(:vTentativa, fn(vista) -> %{vista |
                    num_vista: vista.num_vista + 1, copia: nodo} end)
            end
            ##caso en el que llega un 0 y el sistema tiene primario y copia validos
        end
      Agent.get(:vTentativa, fn vista -> vista end)
    end

    defp procesa_latido(_, -1) do
        ##devuelve la vista tentativa
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    defp procesa_latido(nodo, n_vista) do
        vistaTentativa = Agent.get(:vTentativa, fn(v) -> v end)
        if nodo == vistaTentativa.primario 
                and n_vista == vistaTentativa.num_vista 
                and vistaTentativa.valida == false do
            Agent.update(:vTentativa, fn v -> %{v | valida: true} end)
        end
        Agent.get(:vTentativa, fn vista -> vista end)
    end

    def vista_inicial do
        %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
    end
end
