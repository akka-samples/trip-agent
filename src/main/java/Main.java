import com.lb.application.coordinator.TripCoordinator;

public class Main {
  public static void main(String[] args) {
    String res = """
            Here are the flights that match your criteria, formatted as JSON:
                        
            ```json
            [
              {
                "id": "3",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1746525600,
                "arrival": 1746534600,
                "price": 140
              },
              {
                "id": "9",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1746556200,
                "arrival": 1746565200,
                "price": 145
              },
              {
                "id": "18",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1746637200,
                "arrival": 1746645900,
                "price": 145
              },
              {
                "id": "23",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1747130400,
                "arrival": 1747139400,
                "price": 140
              },
              {
                "id": "29",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1747161000,
                "arrival": 1747170000,
                "price": 145
              },
              {
                "id": "38",
                "from": "Tokyo",
                "to": "Seoul",
                "departure": 1747242000,
                "arrival": 1747250700,
                "price": 145
              }
            ]
            ```
            """;

    System.out.println(TripCoordinator.extractJson(res));

  }
}
