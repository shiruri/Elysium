package elysium;


public class UtilsMath {
    
    public static boolean isNumeric(String numbers) {
        try {
            Double.parseDouble(numbers);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }
    
    public static int precedenceLevel(char op) {
        switch (op) {
            case '(':
            case ')':
                return 0;
            case '*':
            case '/':
                return 4;
            case '+':
            case '-':
                return 3;
           
            default:
                throw new IllegalArgumentException("Operator unknown: " + op);
        }
    }


}
