package caghost.tools;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Main {

    private static int distance(List<Integer> l) {
        return Math.abs(l.get(0) - l.get(1));
    }

    public static void main(String[] args) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        var l = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(10))
                .limit(5)
                .boxed()
                .collect(toList());
        List<List<Integer>> tuples = new ArrayList<>();
        for(int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.size(); j++) {
                if(i == j) continue;
                tuples.add(List.of(i,j));
            }
        }
        System.out.println(tuples);
    }
}
