package kdl;

import kdl.parse.KDLParser;

public class Fuzzer {
    public static void main(String[] args) {
        final KDLParser parser = new KDLParser();

        try {
            parser.parse(System.in);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
