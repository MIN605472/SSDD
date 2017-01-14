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
    def envio_latidos(nodo_servidor_gv, num_vista) do
        {vista, _} = ClienteGV.latido(nodo_servidor_gv, num_vista)
        Process.sleep(@intervalo_latido)

        #Distinción de casos: primario, copia, espera...
        #vista.primario == Node.self()
        ##

        vista_guardada = Agent.get(:vistaTentativa, fn x -> x)

        if vista_guardada.copia != vista.copia do
            realizar_copia(vista.copia)
        end

        if vista.primario == Node.self() and not copiado do
            realizar_copia(vista.copia)
        end


        IO.inspect(vista.num_vista)
        if vista.num_vista == 1 do
            Agent.update(:vistaTentativa, fn _ -> vista end)
            envio_latidos(nodo_servidor_gv, -1)
        else
            if(#copiado) do
                envio_latidos(nodo_servidor_gv, vista.num_vista)
            else
                envio_latidos(nodo_servidor_gv, vista.num_vista-1)
            end
        end

    end

    def init_sa(nodo_servidor_gv) do
        Process.register(self(), :servidor_sa)
        # Process.register(self(), :cliente_gv)       


    #------------- INICIALIZACION ..........
        Agent.start_link(fn -> Map.new() end, name: :diccionario)
        #vistaTentativa = %ServidorGV{num_vista: -1, primario: :undefined, copia: :undefined}
        Agent.start_link(fn -> :empty end, name: :vistaTentativa)
        pid = spawn fn -> envio_latidos(nodo_servidor_gv, 0) end

         # Poner estado inicial
        bucle_recepcion_principal() 
    end


    defp bucle_recepcion_principal() do
        receive do
            {_, primario, copia} = Agent.get(:vistaTentativa, fn x -> x end)
            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {:lee, clave, nodo_origen}  ->  
                send(nodo_origen, {:resultado, lee_diccionario(clave)})

            {:escribe_generico, {clave, valor, hash?}, nodo_origen} ->
                if hash? do
                    valor = hash(valor)
                end 
                exito = escribe_copia(clave, valor)                
                if exito do
                    if (copia == Node.self() and nodo_origen == primario) or primario == Node.self() do
                        escribe_diccionario(clave, valor)
                        send(nodo_origen,{:resultado, valor})
                    else
                        send(nodo_origen, {:resultado, :no_soy_primario_valido})
                    end
                else
                    send(nodo_origen, :fallo)
                end
                # ----------------- vuestro códio


        # --------------- OTROS MENSAJES QE NECESITEIS


        end
        bucle_recepcion_principal()
    end

    defp escribe_copia(clave, valor) do
        {_, _, copia} = Agent.get(:vistaTentativa, fn x -> x end)
        if copia != Node.self() do
            send({:servidor_sa, copia}, 
                {:escribe_generico, {clave, valor, false}, self()})
            receive do
                {:resultado, valor} -> true
                otro -> false
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