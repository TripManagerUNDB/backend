package com.undb.TripManagerUNDB.itinerary.service;

import com.undb.TripManagerUNDB.itinerary.entity.ItineraryDay;
import com.undb.TripManagerUNDB.itinerary.repository.ItineraryRepository;
import com.undb.TripManagerUNDB.trip.entity.Trip;
import com.undb.TripManagerUNDB.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripService tripService;

    private static final DateTimeFormatter BR_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM", new Locale("pt", "BR"));

    // Banco de atividades por destino / tipo
    private static final Map<String, List<ItineraryDay.Activity>> DEST_ACTIVITIES = Map.ofEntries(
        Map.entry("Paris", List.of(
            act("09:00","Torre Eiffel",      "Monumento",    "🗼","2h",  120,"Chegue antes das 9h para evitar filas. Reserve ingresso online."),
            act("12:00","Café de Flore",     "Restaurante",  "☕","1h",   85,"Café histórico em Saint-Germain. Prove o café com croissant."),
            act("14:00","Musée du Louvre",   "Museu",        "🎨","3h",   95,"Foque na ala grega e na Mona Lisa. Baixe o app do museu."),
            act("19:00","Le Comptoir",       "Restaurante",  "🍽️","2h", 180,"Bistrô clássico. Reserve com antecedência."),
            act("10:00","Marais District",   "Bairro",       "🏛️","3h",   0,"Explore galerias e a Place des Vosges."),
            act("14:00","Centre Pompidou",   "Museu",        "🎭","2h",   75,"Arte moderna. Vista incrível do terraço."),
            act("09:30","Versalhes",         "Palácio",      "🏰","5h", 240,"Excursão de dia inteiro. Reserve guia para o Trianon."),
            act("10:00","Montmartre",        "Bairro",       "🎨","3h",   0,"Suba a pé e visite ateliês de artistas."),
            act("13:30","Sacré-Cœur",        "Igreja",       "⛪","1h",   0,"Vista panorâmica de 360°. Entrada gratuita."),
            act("16:00","Galeries Lafayette","Compras",      "🛍️","2h", 300,"Dôme art nouveau e rooftop com vista para a Ópera.")
        )),
        Map.entry("Tóquio", List.of(
            act("09:00","Senso-ji",          "Templo",       "⛩️","2h",   0,"Chegue cedo para ver o templo sem multidões."),
            act("12:00","Ramen Ichiran",     "Restaurante",  "🍜","1h",  60,"Ramen em cabines individuais — experiência única."),
            act("14:00","Shibuya Crossing",  "Bairro",       "🚶","2h",   0,"O cruzamento mais movimentado do mundo. Observe do Starbucks."),
            act("19:00","Izakaya local",     "Restaurante",  "🍣","2h", 120,"Jantar em izakaya autêntico com yakitori e sake."),
            act("10:00","TeamLab Planets",   "Museu",        "🎨","3h", 180,"Arte digital imersiva. Reserve com semanas de antecedência."),
            act("09:00","Shinjuku Gyoen",    "Natureza",     "🌸","2h",  15,"Jardim nacional perfeito para caminhadas tranquilas."),
            act("14:00","Harajuku",          "Compras",      "🛍️","2h",   0,"Moda jovem e lojas exclusivas na Takeshita Street."),
            act("10:00","Tsukiji Fish Market","Mercado",     "🐟","2h",   0,"Mercado externo ainda em operação. Chegue cedo para o café."),
            act("15:00","Akihabara",         "Bairro",       "🎮","3h",   0,"Paraíso eletrônico e cultura pop japonesa.")
        )),
        Map.entry("Islândia", List.of(
            act("08:00","Golden Circle",     "Natureza",     "🌋","6h", 150,"Tour pelo Geysir, Gullfoss e Þingvellir. Alugar carro é essencial."),
            act("20:00","Aurora Boreal",     "Natureza",     "🌌","3h",   0,"Check app Aurora Forecast. Afaste-se das luzes da cidade."),
            act("09:00","Lagoa Azul",        "Spa",          "💆","4h", 280,"Reserve com meses de antecedência. Leve óculos para a sílica."),
            act("10:00","Skógafoss",         "Natureza",     "🏔️","3h",   0,"Cachoeira icônica. Suba os 400 degraus para vista privilegiada."),
            act("09:00","Jökulsárlón",       "Natureza",     "🧊","4h",   0,"Lagoa glacial com blocos de gelo flutuantes — surreal."),
            act("14:00","Vik",               "Bairro",       "🖤","2h",   0,"Praia de areia negra vulcânica. Cuidado com ondas.")
        )),
        Map.entry("Marrocos", List.of(
            act("09:00","Medina de Fez",     "Bairro",       "🕌","4h",   0,"A medina mais antiga do mundo. Contrate guia local."),
            act("14:00","Curtumes Chouara",  "Monumento",    "🎨","1h",  30,"Vista dos curtumes de couro do terraço das lojas."),
            act("09:00","Mercado Jemaa",     "Mercado",      "🏺","3h",   0,"Praça principal de Marrakech. Visite ao anoitecer."),
            act("14:00","Palácio Bahia",     "Palácio",      "🏰","2h",  15,"Arquitetura islâmica impressionante do século XIX."),
            act("08:00","Deserto Sahara",    "Natureza",     "🐪","8h", 350,"Passeio de camelo ao amanhecer. Inclui pernoite opcional.")
        )),
        Map.entry("Lisboa", List.of(
            act("09:00","Pastéis de Belém",  "Restaurante",  "🍮","1h",  20,"O original pastel de nata desde 1837. Fila compensa."),
            act("11:00","Mosteiro Jerônimos","Monumento",    "🏛️","2h",  15,"Obra máxima do Manuelino. Tumba de Vasco da Gama."),
            act("14:00","Alfama",            "Bairro",       "🎸","3h",   0,"Bairro histórico. Ouça fado ao vivo num tasca."),
            act("10:00","Sintra",            "Natureza",     "🏰","6h",  35,"Palácio da Pena e Castelo dos Mouros. Ida de trem."),
            act("19:00","Time Out Market",   "Restaurante",  "🍷","2h", 100,"Melhor gastronomia portuguesa num só lugar.")
        ))
    );

    // Atividades genéricas para destinos sem mapeamento
    private static final List<ItineraryDay.Activity> GENERIC_ACTIVITIES = List.of(
        act("09:00","Centro Histórico",   "Bairro",      "🏙️","3h",   0,"Explore o centro da cidade a pé."),
        act("12:30","Almoço local",       "Restaurante", "🍽️","1h",  80,"Experimente a gastronomia típica da região."),
        act("14:30","Museu Principal",    "Museu",       "🎨","2h",  50,"Visite o museu mais importante da cidade."),
        act("17:00","Parque / Jardim",    "Natureza",    "🌿","2h",   0,"Descanso e fotos no parque central."),
        act("20:00","Jantar típico",      "Restaurante", "🍷","2h", 120,"Jantar com pratos regionais e vinho local."),
        act("10:00","Mercado Local",      "Mercado",     "🛍️","2h",   0,"Explore o mercado e compre lembranças."),
        act("14:00","Tour Histórico",     "Monumento",   "🗺️","3h",  60,"Passeio guiado pelos pontos históricos."),
        act("09:00","Mirante / Vista",    "Natureza",    "📸","2h",   0,"Vista panorâmica da cidade. Chegue cedo para luz perfeita."),
        act("15:00","Bairro Boêmio",      "Bairro",      "🎭","2h",   0,"Cafés, arte de rua e lojas alternativas."),
        act("19:00","Pôr do Sol",         "Natureza",    "🌅","1h",   0,"Melhor spot para apreciar o pôr do sol local.")
    );

    public List<ItineraryDay> generate(String userId, String tripId) {
        Trip trip = tripService.findEntityById(tripId);

        if (!trip.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Acesso negado.");
        }

        itineraryRepository.deleteByTripId(tripId);

        // Seleciona banco de atividades do destino ou usa genérico
        List<ItineraryDay.Activity> pool = DEST_ACTIVITIES.entrySet().stream()
                .filter(e -> trip.getDestination().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(GENERIC_ACTIVITIES);

        List<ItineraryDay> days = new ArrayList<>();
        LocalDate current = trip.getCheckIn();
        int dayNum = 1;
        int actOffset = 0;

        while (!current.isAfter(trip.getCheckOut().minusDays(1))) {
            // 3-4 atividades por dia, rotacionando o pool
            int count = (dayNum % 2 == 0) ? 3 : 4;
            List<ItineraryDay.Activity> dayActs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                dayActs.add(pool.get(actOffset % pool.size()));
                actOffset++;
            }

            ItineraryDay day = ItineraryDay.builder()
                    .tripId(tripId)
                    .dayNumber(dayNum)
                    .date(current.format(BR_FORMAT))
                    .activities(dayActs)
                    .build();

            days.add(itineraryRepository.save(day));
            current = current.plusDays(1);
            dayNum++;
        }

        return days;
    }

    public List<ItineraryDay> getDays(String tripId) {
        return itineraryRepository.findByTripIdOrderByDayNumber(tripId);
    }

    public ItineraryDay updateActivity(String dayId, int index, ItineraryDay.Activity updated) {
        ItineraryDay day = findDay(dayId);
        if (index < 0 || index >= day.getActivities().size()) {
            throw new IllegalArgumentException("Índice de atividade inválido.");
        }
        day.getActivities().set(index, updated);
        return itineraryRepository.save(day);
    }

    public ItineraryDay removeActivity(String dayId, int index) {
        ItineraryDay day = findDay(dayId);
        if (index < 0 || index >= day.getActivities().size()) {
            throw new IllegalArgumentException("Índice de atividade inválido.");
        }
        day.getActivities().remove(index);
        return itineraryRepository.save(day);
    }

    private ItineraryDay findDay(String dayId) {
        return itineraryRepository.findById(dayId)
                .orElseThrow(() -> new NoSuchElementException("Dia não encontrado: " + dayId));
    }

    private static ItineraryDay.Activity act(String time, String name, String type,
                                              String icon, String dur, int cost, String desc) {
        return ItineraryDay.Activity.builder()
                .time(time).name(name).type(type)
                .icon(icon).dur(dur).cost(cost).desc(desc)
                .build();
    }
}
