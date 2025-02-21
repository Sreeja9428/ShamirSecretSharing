import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretSharing {

    // Decodes JSON values into a map of (x, y) points
    public static Map<Integer, BigInteger> decodeValues(JSONObject testCase) {
        Map<Integer, BigInteger> dataPoints = new HashMap<>();

        for (String key : testCase.keySet()) {
            if (key.equals("keys")) continue; // Skip metadata

            int x = Integer.parseInt(key);
            JSONObject valueObj = testCase.getJSONObject(key);
            int base = valueObj.getInt("base");
            BigInteger y = new BigInteger(valueObj.getString("value"), base); // Using BigInteger

            dataPoints.put(x, y);
        }
        return dataPoints;
    }

    // Lagrange Interpolation for reconstructing the secret
    public static BigInteger lagrangeInterpolation(int[] xValues, BigInteger[] yValues) {
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < xValues.length; i++) {
            BigInteger term = yValues[i];
            BigInteger num = BigInteger.ONE;
            BigInteger denom = BigInteger.ONE;

            for (int j = 0; j < xValues.length; j++) {
                if (i != j) {
                    num = num.multiply(BigInteger.valueOf(-xValues[j]));
                    denom = denom.multiply(BigInteger.valueOf(xValues[i] - xValues[j]));
                }
            }

            term = term.multiply(num).divide(denom);
            secret = secret.add(term);
        }
        return secret;
    }

    // Reads a JSON file and computes the secret
    public static BigInteger solvePolynomialFromJson(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject testCase = new JSONObject(content);

        int k = testCase.getJSONObject("keys").getInt("k"); // Number of required shares

        Map<Integer, BigInteger> dataPoints = decodeValues(testCase);

        List<Integer> xList = new ArrayList<>(dataPoints.keySet());
        List<BigInteger> yList = new ArrayList<>(dataPoints.values());

        int[] xValues = new int[k];
        BigInteger[] yValues = new BigInteger[k];

        for (int i = 0; i < k; i++) {
            xValues[i] = xList.get(i);
            yValues[i] = yList.get(i);
        }

        return lagrangeInterpolation(xValues, yValues);
    }

    // Main function to run the test cases
    public static void main(String[] args) throws IOException {
        String[] testCaseFiles = {"testcase1.json", "testcase2.json"};

        for (String file : testCaseFiles) {
            System.out.println("Secret for " + file + ": " + solvePolynomialFromJson(file));
        }
    }
}
