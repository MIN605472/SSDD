defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    #@enforce_keys [:nVista]
    defstruct  [:nVista, :idPrimary, :idCopy]
    end   # A COMPLETAR

    @periodo_latido 50

    @latidos_fallidos 4

    

    @doc """
        Poner en marcha el servidor para gestión de vistas
    """
    @spec start(String.t, String.t) :: atom
    def start(host, nombre_nodo) do
        nodo = NodoRemoto.start(host, nombre_nodo,__ENV__.file, __MODULE__)

        Node.spawn_link(nodo, __MODULE__, :init_sv, [])

        nodo
    end


    #------------------- Funciones privadas
    # Estas 2 primeras deben ser defs para llamadas tipo (MODULE, funcion,[])
    def init_sv() do
        Process.register(self(), :servidor_gv)

        spawn(__MODULE__, :init_monitor, [self()]) # otro proceso concurrente

        vistaValida = %ServidorGV{nVista: 0, idPrimary: nil, idCopy: nil}
        vistaTentativa = %ServidorGV{nVista: 0, idPrimary: nil, idCopy: nil}
        Agent.start_link(fn -> vistaValida end, name: vValida)
        Agent.start_link(fn -> vistaTentativa end, name: vTentativa)
        Agent.start_link(fn -> Map.new() end, name: lista_latidos)
        #### VUESTRO CODIGO DE INICIALIZACION

        bucle_recepcion(vistaTentativa, set1)
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido*@latidos_fallidos)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion(nueva_vista, lista_latidos) do
        nueva_vista = receive do
                    {:latido, nodo_origen, n_vista} ->
                        procesa_latido(nodo_origen, n_vista)
                        update(lista_latidos, nodo_origen, 0, fn(x) -> x+1)
                        ### VUESTRO CODIGO
                
                    {:obten_vista, pid} ->

                        ### VUESTRO CODIGO                

                    :procesa_situacion_servidores ->
                        procesar_situacion_servidores(lista_latidos)
                        ### VUESTRO CODIGO

        end

        bucle_recepcion(nueva_vista, lista_latidos)
    end
    ##SI CAE NODO, PONER EN ESTRUCTURA A NIL
    defp procesar_situacion_servidores(lista_latidos) do
        nodoP=Agent.get(vTentativa, fn(vista) -> vista.idPrimary end)
        nodoR=Agent.get(vTentativa, fn(vista) -> vista.idCopy end)
        {latidosP,lista_latidos} = pop(lista_latidos, nodoP)
        {latidosR,lista_latidos} = pop(lista_latidos, nodoR)



        #### VUESTRO CODIGO
        
    end



    defp procesa_latido(nodo, 0) do
        vistaTentativa = Agent.get(vTentativa, fn(vista) -> vista end)
        ##si recibimos un 0 y no hay primario
        if vistaTentativa.idPrimary == nil do
            Agent.update(vTentativa, fn(vista) -> %{vista | 
                nVista: vista.nVista+1, idPrimary: nodo} end)        
        else
            ##si recibimos un 0 y hay primario pero no hay copia
            if vistaTentativa.idCopy == nil do
                Agent.update(vTentativa, fn(vista) -> %{vista |
                 nVista: vista.nVista+1, idCopy: nodo} end)
            end
            ##caso en el que llega un 0 y el sistema tiene primario y copia validos
        end
    end

    defp procesa_latido(nodo, 1) do
        ## si recibimos un 1 validamos la vista tentativa
        vistaTentativa = Agent.get(vTentativa, fn(vista) -> vista end)
        Agent.update(vValida, fn(vista) -> %{vista | nVista: vistaTentativa.nVista , 
            idPrimary: vistaTentativa.idPrimary, idCopy: vistaTentativa.idCopy} end)
    end

    defp procesa_latido(nodo, -1) do
        ##devuelve la vista tentativa
    end

    defp procesa_latido(nodo, vista) do        
        if vistaTentativa.idCopy == nil do
                Agent.update(vTentativa, fn(vista) -> %{vista |
                 nVista: vista.nVista+1, idCopy: nodo} end)
            end
    end
end