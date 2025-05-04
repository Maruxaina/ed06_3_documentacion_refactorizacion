package org.ed06.model;

import java.time.LocalDate;
import java.util.*;

/**
 * Representa un hotel con habitacións, clientes e reservas.
 */
public class Hotel {
    private String nombre;
    private String direccion;
    private String telefono;

    private final Map<Integer,Cliente> clientes = new HashMap<>();
    private final List<Habitacion> habitaciones = new ArrayList<>();
    private final Map<Integer,List<Reserva>> reservasPorHabitacion = new HashMap<>();

    /**
     * Constructor para crear unha nova instancia da clase Hotel.
     *
     * @param nombre O nome do hotel.
     * @param direccion A dirección do hotel.
     * @param telefono O número de teléfono do hotel.
     */
    public Hotel(String nombre, String direccion, String telefono) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    /**
     * Rexistra unha nova habitación no hotel.
     *
     * @param tipo O tipo de habitación (ex: Individual, Dobre, Suite).
     * @param precioBase O precio base por noite da habitación.
     */
    // Método para agregar una nueva habitación al hotel
    public void registrarHabitacion(String tipo, double precioBase) {
        Habitacion habitacion = new Habitacion(habitaciones.size() + 1, tipo, precioBase);
        habitaciones.add(habitacion);
        reservasPorHabitacion.put(habitacion.getNumero(), new ArrayList<>());
    }

    /**
     * Rexistra múltiples habitacións no hotel.
     *
     * @param tipos Unha lista de tipos de habitacións.
     * @param preciosBase Unha lista de prezos base correspondentes aos tipos de habitacións.
     */
    public void registrarHabitaciones(List<String> tipos, List<Double> preciosBase) {
        for(int i = 0; i < tipos.size(); i++) {
            Habitacion habitacion = new Habitacion(habitaciones.size() + 1, tipos.get(i), preciosBase.get(i));
            habitaciones.add(habitacion);
            reservasPorHabitacion.put(habitacion.getNumero(), new ArrayList<>());
        }
    }

    /**
     * Lista as habitacións dispoñibles no hotel.
     */
    public void listarHabitacionesDisponibles() {
        for(Habitacion habitacion : habitaciones) {
            if(habitacion.isDisponible()) {
                System.out.println("Habitación #" + habitacion.getNumero() + " - Tipo: " + habitacion.getTipo() + " - Precio base: " + habitacion.getPrecioBase());
            }
        }
    }

    /**
     * Obtén unha habitación específica polo seu número.
     *
     * @param numero O número da habitación a buscar.
     * @return A instancia da clase Habitación se a atopa, ou null se non existe.
     */
    public Habitacion getHabitacion(int numero) {
        for(Habitacion habitacion : habitaciones) {
            if(habitacion.getNumero() == numero) {
                return habitacion;
            }
        }
        return null;
    }

    /**
     * Realiza unha reserva de habitación para un cliente específico.
     * Comproba a dispoñibilidade, a existencia do cliente e a coherencia das datas de entrada e saída.
     * Se atopa unha habitación dispoñible do tipo solicitado, crea unha nova reserva.
     * Tamén verifica si o cliente se transforma en VIP tras a nova reserva.
     *
     * @param clienteId O id do cliente que realiza a reserva.
     * @param tipo O tipo de habitación solicitado.
     * @param fechaEntrada A data de entrada da reserva.
     * @param fechaSalida A data de saída da reserva.
     * @return O número da habitación reservada se a reserva se fai con éxito,
     * -1 se non hai habitacións dispoñibles do tipo solicitado,
     * -2 se a data de entrada e posterior á de saída,
     * -3 se non existe o cliente co ID proporcionado,
     * -4 se non hai habitacións no hotel.
     */
    //Método para realizar una reserva.
    // Comprueba si hay habitaciones disponibles, si existe el cliente y si las fechas son coherentes.
    // Si encuentra una habitación disponible del tipo solicitado,
    // crea una nueva reserva y la añade a la lista de reservas y devuelve el número de la habitación reservada.
    // Antes de crear la reserva, comprueba si el cliente pasa a ser VIP tras la nueva reserva,
    // en caso de que haya realizado más de 3 reservas en el último año.
    public int reservarHabitacion(int clienteId, String tipo, LocalDate fechaEntrada, LocalDate fechaSalida) {
        // Comprobamos si hay habitaciones en el hotel
        if(!habitaciones.isEmpty()) {
            //comprobamos si existe el cliente
            if(this.clientes.get(clienteId) != null) {
                Cliente cliente = this.clientes.get(clienteId);
                // comprobamos si las fechas son coherentes
                if(fechaEntrada.isBefore(fechaSalida)) {
                    //buscamos una habitación disponible
                    for(Habitacion habitacion : habitaciones) {
                        if(habitacion.getTipo().equals(tipo.toUpperCase()) && habitacion.isDisponible()) {
                            // Comprobamos si el cliente pasa a ser vip tras la nueva reserva
                            int numReservas = 0;
                            for (List<Reserva> reservasHabitacion : reservasPorHabitacion.values()) {
                                for(Reserva reservaCliente : reservasHabitacion) {
                                    if(reservaCliente.getCliente().equals(cliente)) {
                                        if(reservaCliente.getFechaInicio().isAfter(LocalDate.now().minusYears(1))) {
                                            numReservas++;
                                        }
                                    }
                                }
                            }
                            if(numReservas > 3 && !cliente.esVip) {
                                cliente.esVip = true;
                                System.out.println("El cliente " + cliente.nombre + " ha pasado a ser VIP");
                            }

                            // Creamos la reserva
                            Reserva reserva = new Reserva(reservasPorHabitacion.size() + 1, habitacion, cliente, fechaEntrada, fechaSalida);
                            reservasPorHabitacion.get(habitacion.getNumero()).add(reserva);
                            // Marcamos la habitación como no disponible
                            habitacion.reservar();

                            System.out.println("Reserva realizada con éxito");
                            return habitacion.getNumero();
                        }
                    }
                    // si no hay habitaciones disponibles del tipo solicitado, mostramos un mensaje
                    System.out.println("No hay habitaciones disponibles del tipo " + tipo);
                    return -1;
                } else {
                    System.out.println("La fecha de entrada es posterior a la fecha de salida");
                    return -2;
                }
            } else {
                System.out.println("No existe el cliente con id " + clienteId);
                return -3;
            }
        } else {
            System.out.println("No hay habitaciones en el hotel");
            return -4;
        }

    }

    /**
     * Lista todas as reservas realizadas no hotel, agrupadas por habitación.
     */
    public void listarReservas() {
        reservasPorHabitacion.forEach((key, value) -> {
            System.out.println("Habitación #" + key);
            value.forEach(reserva -> System.out.println(
                "Reserva #" + reserva.getId() + " - Cliente: " + reserva.getCliente().nombre
                    + " - Fecha de entrada: " + reserva.getFechaInicio()
                    + " - Fecha de salida: " + reserva.getFechaFin()));
        });
    }

    /**
     * Lista todos os clientes rexistrados no hotel.
     */
    public void listarClientes() {
        for(Cliente cliente : clientes.values()) {
            System.out.println("Cliente #" + cliente.id + " - Nombre: " + cliente.nombre + " - DNI: " + cliente.dni + " - VIP: " + cliente.esVip);
        }
    }

    /**
     * Rexistra un novo cliente no hotel.
     *
     * @param nombre O nome do cliente.
     * @param email O correo electrónico do cliente.
     * @param dni O número do DNI do cliente.
     * @param esVip Indica se o cliente é VIP.
     */
    public void registrarCliente(String nombre, String email, String dni, boolean esVip) {
        Cliente cliente = new Cliente(clientes.size() + 1, nombre, dni, email, esVip);
        clientes.put(cliente.id, cliente);
    }
}
