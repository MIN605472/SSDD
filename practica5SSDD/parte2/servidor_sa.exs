Code.require_file("#{__DIR__}/cliente_gv.exs")

defmodule ServidorSA do
    
                
    defstruct [:num_vista, :primario, :copia] 


    @intervalo_latido 50


    @doc """
        Obtener el hash de un string Elixir
            - Necesario pasar, previamente,  a formato string Erlang
         - Devuelve entero
    """
    def hash(string_concatenado) do
        String.to_charlist(string_concatenado) |> :erlang.phash2
    end


    @doc """
        Poner en marcha un servidor de almacenamiento
    """
    @spec start(String.t, String.t, node) :: node
    def start(host, nombre_nodo, nodo_servidor_gv) do
        nodo = NodoRemoto.start(host, nombre_nodo, __ENV__.file, __MODULE__)
        
        Node.spawn(nodo, __MODULE__, :init_sa, [nodo_servidor_gv])

        nodo
    end


    #------------------- Funciones privadas -----------------------------
    defp realizar_copia(nodo_destino) do
        backup = Agent.get(:diccionario, fn x -> x end)
        send(nodo_destino,{:escribe_backup, backup, Node.self()})
        receive do
            {:copia_realizada, realizada} -> realizada

        after @intervalo_latido * 4 ->
            false
        end
    end

    defp latido_primario(vista, vista_valida, vista_guardada, nodo_servidor_gv) do
        if vista_guardada.copia != vista.copia and vista.copia != :undefined 
        and vista_valida.num_vista != 0 do            
            exito = realizar_copia(vista.copia)
            if exito do
                envio_latidos(nodo_servidor_gv, vista.num_vista)
            else
                envio_latidos(nodo_servidor_gv, vista.num_vista-1)
            end            
        end
    end

    defp tratar_latido(vista, vista_valida, nodo_servidor_gv) do
        vista_guardada = Agent.get(:vistaTentativa, fn x -> x end)
        if vista.primario != Node.self() do
            envio_latidos(nodo_servidor_gv, vista.num_vista)
        else
            latido_primario(vista, vista_valida, vista_guardada, nodo_servidor_gv)
            if vista.num_vista == 1 do
                envio_latidos(nodo_servidor_gv, -1)
            end        
        end        
        Agent.update(:vistaTentativa, fn _ -> vista end)
    end

    def envio_latidos(nodo_servidor_gv, num_vista) do
        {vista, _} = ClienteGV.latido(nodo_servidor_gv, num_vista)
        {vista_valida, _} = ClienteGV.obten_vista(nodo_servidor_gv)
        tratar_latido(vista, vista_valida, nodo_servidor_gv)
        Process.sleep(@intervalo_latido)
    end

    def init_sa(nodo_servidor_gv) do
        Process.register(self(), :servidor_sa)

        Agent.start_link(fn -> Map.new() end, name: :diccionario)
        vistaTentativa = %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
        Agent.start_link(fn -> vistaTentativa end, name: :vistaTentativa)
        pid = spawn fn -> envio_latidos(nodo_servidor_gv, 0) end

        bucle_recepcion_principal() 
    end


    defp bucle_recepcion_principal() do
        receive do
            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {:lee, clave, nodo_origen}  ->  
                send(nodo_origen, {:resultado, lee_diccionario(clave)})

            {:escribe_generico, {clave, valor, true}, nodo_origen} ->
                tratar_escritura_hash(clave, valor, nodo_origen)

            {:escribe_generico, {clave, valor, false}, nodo_origen} ->
                tratar_escritura(clave, valor, nodo_origen)

            {:escribe_backup, diccionario, nodo_origen} ->
                backup_copia(diccionario)
                send(nodo_origen, {:copia_realizada, true})

        end
        bucle_recepcion_principal()
    end

    defp backup_copia(diccionario) do
        Agent.update(:diccionario, fn _ -> diccionario end)
    end

    defp tratar_escritura(clave, valor, nodo_origen) do
        {_, primario, copia} = Agent.get(:vistaTentativa, fn x -> x end)
        exito = escribe_copia(clave, valor)                
        if ((copia == Node.self() and nodo_origen == primario) or primario == Node.self()) and exito do
            escribe_diccionario(clave, valor)
            send(nodo_origen,{:resultado, valor})
        else
            send(nodo_origen, {:resultado, :no_soy_primario_valido})
        end
    end

     defp tratar_escritura_hash(clave, valor, nodo_origen) do
        valor_hash = forma_string(clave,valor)
        {_, primario, copia} = Agent.get(:vistaTentativa, fn x -> x end)
        exito = escribe_copia(clave, valor_hash)                
        if ((copia == Node.self() and nodo_origen == primario) or primario == Node.self()) and exito do
            escribe_diccionario(clave, valor_hash)
            send(nodo_origen,{:resultado, valor})
        else
            send(nodo_origen, {:resultado, :no_soy_primario_valido})
        end
    end


    defp forma_string(clave, valor) do
        cadena_previa = lee_diccionario(clave)
        hash(cadena_previa<>valor)
    end

    defp escribe_copia(clave, valor) do
        {_, _, copia} = Agent.get(:vistaTentativa, fn x -> x end)
        if copia != Node.self() do
            send({:servidor_sa, copia}, 
                {:escribe_generico, {clave, valor, false}, self()})
            receive do
                {:resultado, valor} -> true
                otro -> false

                after @intervalo_latido *2 -> false
            end
        else
            true
        end
    end

    @spec lee_diccionario(String.t) :: String.t
    defp lee_diccionario(clave) do
        Agent.get(:diccionario, fn map -> Map.get(map, clave, "") end)        
    end

    defp escribe_diccionario(clave, valor) do
        claves = Agent.get(:diccionario, fn x -> x end)
        nuevas_claves = Map.update(claves, clave, valor, fn _ -> valor end)
        Agent.update(:diccionario, fn _ -> nuevas_claves end)
    end
end