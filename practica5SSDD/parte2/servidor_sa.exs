Code.require_file("#{__DIR__}/cliente_gv.exs")

defmodule ServidorSA do
    
    #No es necesario         
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
    #Envia un mensaje con la copia de los datos al nodo_destino
    defp realizar_copia(nodo_destino) do
        backup = Agent.get(:diccionario, fn x -> x end)
        send({:servidor_sa, nodo_destino},{:escribe_backup, backup, Node.self()})
        receive do
            {:copia_realizada, realizada} -> realizada

        after @intervalo_latido * 4 ->
            false
        end
    end

    #En función de la vista tentativa que tenemos enviamos un latido
    defp generar_latido(nodo_servidor_gv) do
        vista_guardada = Agent.get(:vistaTentativa, fn x -> x end)

        if vista_guardada.primario != Node.self() do
            {vista, _} = ClienteGV.latido(nodo_servidor_gv, vista_guardada.num_vista)
            Agent.update(:vistaTentativa, fn _ -> vista end)       
        else
            if vista_guardada.num_vista == 1 do
                {vista, _} = ClienteGV.latido(nodo_servidor_gv, -1)
                Agent.update(:vistaTentativa, fn _ -> vista end) 
            else
                {vista, _} = ClienteGV.latido(nodo_servidor_gv, vista_guardada.num_vista)
                #Si ha cambiado la copia desde el ultimo latido
                if vista.num_vista != vista_guardada.num_vista and vista.copia != :undefined do
                    exito = realizar_copia(vista.copia)
                    if exito do
                        Agent.update(:vistaTentativa, fn _ -> vista end)
                    end
                end
            end
        end
    end

    #Proceso concurrente que recuerda al principal cuando enviar un latido
    def envio_latidos(pid_principal) do
        send(pid_principal, :enviar_latido)
        Process.sleep(@intervalo_latido)
        envio_latidos(pid_principal)
    end

    #Inicialización del sistema, lanzamos un proceso concurrente para los latidos
    #e inicializamos dos agentes, para los datos y para la vista tentativa
    def init_sa(nodo_servidor_gv) do
        Process.register(self(), :servidor_sa)
        Agent.start_link(fn -> Map.new() end, name: :diccionario)
        vistaTentativa = %ServidorGV{num_vista: 0, primario: :undefined, copia: :undefined}
        Agent.start_link(fn -> vistaTentativa end, name: :vistaTentativa)
        spawn(__MODULE__, :envio_latidos, [self()])
        bucle_recepcion_principal(nodo_servidor_gv) 
    end

    #Bucle de tratamiento de mensajes
    defp bucle_recepcion_principal(nodo_servidor_gv) do

        receive do
            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {:lee, clave, nodo_origen}  ->  
                send({:cliente_sa, nodo_origen}, {:resultado, lee_diccionario(clave)})

            {:escribe_generico, {clave, valor, true}, nodo_origen} ->
                tratar_escritura_hash(clave, valor, nodo_origen, :cliente_sa)

            {:escribe_generico, {clave, valor, false}, nodo_origen} ->
                tratar_escritura(clave, valor, nodo_origen, :cliente_sa)

            {:escribe_generico_b, {clave, valor, false}, nodo_origen} ->
                tratar_escritura(clave, valor, nodo_origen, :servidor_sa)

            {:escribe_backup, diccionario, nodo_origen} ->
                backup_copia(diccionario)
                send({:servidor_sa, nodo_origen}, {:copia_realizada, true})

            :enviar_latido -> generar_latido(nodo_servidor_gv)

        end
        bucle_recepcion_principal(nodo_servidor_gv)
    end

    #Actualiza los datos locales con el diccionario que recibe como parametro (para la copia)
    defp backup_copia(diccionario) do
        Agent.update(:diccionario, fn _ -> diccionario end)
    end

    #nodo_origen nos pide escribir clave/valor, quien = :cliente_sa o :servidor_sa
    defp tratar_escritura(clave, valor, nodo_origen, quien) do
        vista_tentativa = Agent.get( :vistaTentativa , fn x -> x end)
        exito = escribe_copia(clave, valor)
        
        if ((vista_tentativa.copia == Node.self() and nodo_origen == vista_tentativa.primario) 
                or vista_tentativa.primario == Node.self()) and exito do
            escribe_diccionario(clave, valor)
            send({quien, nodo_origen}, { :resultado , valor})
        else 
            send({quien, nodo_origen}, { :resultado , :no_soy_primario_valido})
        end
    end

    #nodo_origen nos pide escribir clave/valor hasheado, quien = :cliente_sa o :servidor_sa
    #esta función no la ejecutará nunca un copia
    defp tratar_escritura_hash(clave, valor, nodo_origen, quien) do
        valor_hash = forma_string(clave,valor)
        vista_tentativa = Agent.get(:vistaTentativa, fn x -> x end)
        exito = escribe_copia(clave, valor_hash)                
        if ((vista_tentativa.copia == Node.self() and nodo_origen == vista_tentativa.primario)
                or vista_tentativa.primario == Node.self()) and exito do
            escribe_diccionario(clave, valor_hash)
            send({quien, nodo_origen}, { :resultado , valor})
        else
            send({quien, nodo_origen}, { :resultado , :no_soy_primario_valido})
        end
    end

    #Creamos el hash con el valor previo de clave y el nuevo valor
    defp forma_string(clave, valor) do
        cadena_previa = lee_diccionario(clave)
        hash(cadena_previa<>valor)
    end

    #Si somos el nodo primario: enviamos a la copia un mensaje de escritura
    #Si somos el copia: no tenemos que escribir en ningun otro nodo
    defp escribe_copia(clave, valor) do
        vista_tentativa = Agent.get(:vistaTentativa, fn x -> x end)
        if vista_tentativa.copia != Node.self() do
            send({:servidor_sa, vista_tentativa.copia}, 
                {:escribe_generico_b, {clave, valor, false}, Node.self()})
            receive do
                {:resultado, valor} -> true
                otro -> false

                after @intervalo_latido *2 -> false
            end
        else
            true
        end
    end

    #Devuelve el valor asociado a clave
    @spec lee_diccionario(String.t) :: String.t
    defp lee_diccionario(clave) do
        Agent.get(:diccionario, fn map -> Map.get(map, clave, "") end)        
    end

    #Modifica el valor asociado a clave o lo inicializa con valor
    defp escribe_diccionario(clave, valor) do
        claves = Agent.get(:diccionario, fn x -> x end)
        nuevas_claves = Map.update(claves, clave, valor, fn _ -> valor end)
        Agent.update(:diccionario, fn _ -> nuevas_claves end)
    end
end