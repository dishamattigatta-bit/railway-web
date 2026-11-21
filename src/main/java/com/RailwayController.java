package com;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.*;

@Controller
public class RailwayController {

    private final Map<String, Map<String, Integer>> graph = new HashMap<>();

    public RailwayController() {
        addEdge("KSR Bengaluru", "Yeshwanthpur", 6);
        addEdge("Yeshwanthpur", "Hebbal", 5);
        addEdge("Bengaluru", "Hebbal", 4);
        addEdge("Hebbal", "Yelahanka", 8);
        addEdge("Yelahanka", "Dodballapur", 15);
        addEdge("Yelahanka", "Chikkaballapur", 28);
        addEdge("Dodballapur", "Gauribidanur", 22);
        addEdge("Gauribidanur", "Hindupur", 21);
        addEdge("Hindupur", "Dharmavaram", 30);
        addEdge("Dharmavaram", "Anantapur", 38);
        addEdge("Anantapur", "Guntakal", 45);
        addEdge("Guntakal", "Ballari", 52);
    }

    private void addEdge(String a, String b, int km) {
        graph.putIfAbsent(a, new HashMap<>());
        graph.putIfAbsent(b, new HashMap<>());
        graph.get(a).put(b, km);
        graph.get(b).put(a, km);
    }

    // This sends all station names to the webpage dropdowns
    @GetMapping("/stations")
    @ResponseBody
    public Set<String> getStations() {
        return graph.keySet();
    }

    // This calculates the shortest route using Dijkstra algorithm
    @GetMapping("/route")
    @ResponseBody
    public Map<String, Object> findRoute(@RequestParam String from, @RequestParam String to) {
        Map<String, Object> response = new HashMap<>();

        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            response.put("error", "One or both stations not found");
            return response;
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> Integer.parseInt(a[1])));

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }

        distances.put(from, 0);
        pq.add(new String[]{from, "0"});

        while (!pq.isEmpty()) {
            String[] current = pq.poll();
            String u = current[0];
            int distU = Integer.parseInt(current[1]);

            if (distU > distances.get(u)) continue;
            if (u.equals(to)) break;

            if (graph.containsKey(u)) {
                for (Map.Entry<String, Integer> neighbor : graph.get(u).entrySet()) {
                    String v = neighbor.getKey();
                    int weight = neighbor.getValue();
                    int alt = distU + weight;

                    if (alt < distances.get(v)) {
                        distances.put(v, alt);
                        previous.put(v, u);
                        pq.add(new String[]{v, String.valueOf(alt)});
                    }
                }
            }
        }

        if (distances.get(to) == Integer.MAX_VALUE) {
            response.put("error", "No route found between selected stations");
            return response;
        }

        List<String> path = new ArrayList<>();
        for (String at = to; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        response.put("path", path);
        response.put("totalDistance", distances.get(to));
        return response;
    }
}