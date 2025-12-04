
package elysium;

import elysium.UtilsMath;
import elysium.Var.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 * @author Shiro
 */

public class Elysium {

    /**
     * main way of program
     */
    // VARIABLES
    //for lexer
    private static ArrayList tokens = new ArrayList();
    //for math expressions 

    // for variables
    private static HashMap<String, String> symbol = new HashMap<>(); // dictionary for variables

    // for input
    private static Scanner scan = new Scanner(System.in);
    private static int lineNumber = 1; // will hold the currentLine for error detection

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: ely <file.cy>");
            System.exit(64);
        } else if (args.length == 1) {

            runFile(args[0]);

        } else {
            runPrompt();
        }

    }

    private static void runFile(String path) {
        // reads the file
        FileInputStream input;
        try {
            //input = new FileInputStream(path); // get the file path of the argument
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            run(new String(bytes, Charset.defaultCharset()));
        } catch (Exception ex) {
            System.getLogger("ERROR: NO SUCH FILE FOUND");
        }

    }

    private static void run(String source) {
        String lines[] = source.split("\n"); // spluts the string into lines
        try {
            if (source.isBlank()) { // if the source is blank or empty
                throw new Exception("Reached the end of line"); // log the detail
            } else {
                for (String line : lines) {
                    // loop through thr whole file
                    lexer(line + "<EOF>"); // checks the layout
                }
                parse(tokens); // add meaning
                tokens.clear();
            }

        } catch (Exception e) { // will catch all error 
            System.out.println("Erorr at: " + lineNumber + " " + e.toString().replace("java.", "")); // prints the error message and lineNumber
        }

    }

    @SuppressWarnings("unchecked")
    private static ArrayList lexer(String fileContent) {
        //  long start = System.nanoTime();
        // variables 
        String tok = ""; // tokens or keywords 
        int state = 0; // states if found keywords
        String string = ""; // literal string to be caught
        String expr = ""; // math expressions
        String n = ""; //will hold numbers
        boolean isExpr = false; // will be the flag for storing expresion
        boolean varStarted = false; // will be the flag for storing var

        String var = ""; // will hold variables

        ArrayList<Character> lists = new ArrayList<>(); // will hold the tokens

        fileContent.trim(); // remove whitespaces

        // add the contents into the list
        for (int i = 0; i < fileContent.length(); i++) {
            lists.add(fileContent.charAt(i));
        }
        int concatCounter = 0 ;
        for (int i = 0; i < lists.size(); i++) {
            char chars = lists.get(i);
            tok += lists.get(i); // append each chars to tok
         //   System.out.println(tok);
            if (" ".equals(tok) || tok.isEmpty()) { // if the tok is empty 
                if (!var.isEmpty() && varStarted == true && (lists.size() > i + 2) && lists.get(i + 1).equals("=") && !lists.get(i + 1).equals('+')) {
                    tokens.add("VAR:" + var);
                    var = "";
                    varStarted = false;
                }else if ((i + 3 < lists.size()) && lists.get(i + 3).toString().equals("@") && isExpr == true) {
                    tokens.add("EXPR:" + expr);
                    expr = "";
                    isExpr = false;
                    tok = "";
                }

                if (!expr.isBlank() && isExpr == false && state != 1 && (i + 1 > tokens.size()) && tokens.getLast() == "IF") { // if last token is an if append number
                    tokens.add("NUM:" + expr);
                    expr = ""; // reset the expr
                    tok = "";
                } else if (state == 0) { // if state is zero meaning no found string
                    tok = ""; // set to empty
                } else { // if state is not 0 append the blank or space into the string
                    tok = " "; // reset token
                }

            } else if (tok.matches("\\R") || tok.equals("<EOF>")) { // if the tok has new  or EOF End of File

                if (!expr.isBlank() && isExpr == true) {  // if expression is not blank and is an expression
                    tokens.add("EXPR:" + expr); // append to the tokens
                    expr = ""; // reset the expression
                    isExpr = false;
                } else if (!expr.isBlank() && isExpr == false && state != 1) { // if its not empty but not an expression
                    tokens.add("NUM:" + expr);
                    expr = ""; // reset the expr
                    //System.out.println("NUM"); // then its a number
                } else if (!var.isEmpty()) { // if its the end of the ;ine and var is not empty
                    tokens.add("VAR:" + var); // add the var to the tokens
                    var = ""; // reset the var
                    varStarted = false;
                }
                tok = ""; // reset token

            } else if (tok.equals("=") && state == 0) { // if tokens is equals
                if ((i + 1 < lists.size()) && (lists.get(i + 1).toString() + tok).equals("==") && var.isBlank()) {  // for adding EQEQ if var is blank and the next token is a = again
                    tokens.add("EQEQ"); // add EQEQ
                    tok = ""; // reset tok
                    i++; // skip the next = to avoid double EQEQ

                } else if (!var.isEmpty()) { // if var is not empty
                    tokens.add("VAR:" + var); // add the var
                    var = ""; // reset the var 
                    varStarted = false; // reset the varStarted flag
                    if ((i + 1 < lists.size()) && !(lists.get(i + 1).toString() + tok).equals("==")) {  // for adding equals variables if the next char is not = again
                        tokens.add("EQUALS"); // add equals
                    }
                    tok = ""; // reset the tok

                } else if ((i > 0) && (lists.get(i - 1).toString() + tok).equals("==")) { // if double equals
                    tokens.add("EQEQ"); // add EQEQ
                    tok = ""; // reset the tok
                    varStarted = false; //set the varSyarted tp fa;se
                    i++; // skip the next equals
                
                } else if ((i + 3 < lists.size()) && (lists.get(i + 3).toString()).equals("@")) { // if double equals
                    tokens.add("EQEQ"); // add EQEQ
                    tok = ""; // reset the tok
                    varStarted = false; //set the varSyarted tp fa;se
                    i++; // skip the next equals
                }

            }else if (tok.equals("@") && state == 0) { // if tok equals @ assignment var 
                varStarted = true; // set the varStarted to true
                var += tok; // append the  @ to the var
                tok = ""; // reset tok
            } else if (varStarted == true) {   // if the varStarted is true
                if (tok.equals("+") || tok.equals("-") || tok.equals("/") || tok.equals("*") || lists.get(i + 1) == ')' || lists.get(i + 1) == '(') {
                   
                   
                    if(!var.isBlank() && lists.get(i + 1) == ')') {
                        var += tok;
                        tok = "";
                        
                    }
                    if (!var.isEmpty() ) { // if its the end of the ;ine and var is not empty
                        tokens.add("VAR:" + var); // add the var to the tokens
                        varStarted = false;
                        var = ""; // reset the var
                        
                        
                        if ((i + 1 < lists.size()) && (tok + lists.get(i + 1).toString()).equals("++") && !tokens.getLast().toString().equals("CONCAT")
                                || (i + 1 < lists.size()) && (tok + lists.get(i - 1).toString()).equals("++") && !tokens.getLast().toString().equals("CONCAT") // for concat if its currently a plus and next is a quote
                                || (i + 1 < lists.size()) && (tok + lists.get(i + 1).toString()).equals("++") &&  !tokens.getLast().toString().equals("CONCAT") 
                                || (i - 1 < lists.size()) && (tok + lists.get(i - 1).toString()).equals("++") &&  !tokens.getLast().toString().equals("CONCAT")  ) { // or next is a @ then concat
                            tokens.add("CONCAT"); // add concat 
                            concatCounter++;
                            isExpr = false;
                            expr = "";
                            i++;
                            tok = ""; // reset valuss

                        }
                    }
                  
                    varStarted = false;
                    var = "";
                    tok = "";
                } else if (tok.equals("<") || tok.equals(">")) {
                    if (!var.isEmpty()) { // if its the end of the ;ine and var is not empty
                        tokens.add("VAR:" + var); // add the var to the tokens
                        varStarted = false;
                        var = ""; // reset the var
                        tok = "";
                    }
                }
                var += tok; // append the vars 
                tok = ""; // reset the tok
            } else if (tok.equalsIgnoreCase("print")) { // if it founds a print keyword
                tokens.add("PRINT"); // append PRINT into the list
                tok = ""; // reset tok

            } else if (tok.equalsIgnoreCase("IF")) { // if it founds a IF keyword
                tokens.add("IF"); // append PRINT into the list
                tok = ""; // reset tok

            } else if (tok.equalsIgnoreCase("THEN")) { // if it founds a THEN keyword
                tokens.add("THEN"); // append PRINT into the list
                tok = ""; // reset tok
            } else if (tok.equalsIgnoreCase("ENDIF")) { // if it founds a THEN keyword
                tokens.add("ENDIF"); // append PRINT into the list
                tok = ""; // reset tok
            } else if (tok.equalsIgnoreCase("input")) { // if tok is input
                tokens.add("INPUT"); // append the input
                tok = ""; // reset the tok
            } else if (Character.isDigit(chars) && state != 1) { // if its a number
                if (isExpr == true) {
                    expr += tok; // if already building expression, add to it
                } else {
                    expr += tok; // otherwise start new number/expression
                }
                tok = ""; // resets token
            } else if (tok.equals("+") || tok.equals("-") || tok.equals("/") || tok.equals("*") || tok.equals("(") || tok.equals(")") || tok.equals("%")) {
                int nextIndex = i + 1;
                if (state == 1) {
                    // still inside string, treat everything literal
                    if (tok.equals("\"")) {
                        tokens.add("STRING:" + string + "\"");
                        state = 0;
                        string = "";
                        tok = "";
                        continue;
                    }
                    string += tok;
                    tok = "";
                    continue;
                }
                while (nextIndex < lists.size() && lists.get(nextIndex) == ' ') {
                    nextIndex++;
                }

                if ((i + 2 < lists.size()) && (tok.equals("+")) && (lists.get(i + 2) == '"' && !tokens.getLast().toString().equals("CONCAT")
                        || (i + 2 < lists.size()) && lists.get(i + 2).toString().equals("@") && !tokens.getLast().toString().equals("CONCAT"))
                        || ((lists.size() > i - 2) && lists.get(i - 2) == '"' &&  !tokens.getLast().toString().equals("CONCAT")
                        || (tokens.size() > i - 2) && tokens.getLast().toString().startsWith("VAR")) && !tokens.getLast().toString().equals("CONCAT")) { // checks the currentToken and if the next is a String
                    if (!expr.isBlank() && isExpr != true) { // if its not empty and not expression
                        tokens.add("NUM:" + expr); // add the number 
                        expr = ""; // reset the expr
                    }if ((i + 1 < lists.size()) && (tok + lists.get(i + 1).toString()).equals("++") && state != 1 || (i - 1 < lists.size()) &&  (tok + lists.get(i - 1).toString()).equals("++") && state != 1) {
                        tokens.add("CONCAT"); // add concat 
                        concatCounter++;
                        i++;
                        tok = ""; // reset tok   
                    }
                   


                } else if ((i + 2 < lists.size()) && lists.get(nextIndex).toString().equals("@") && isExpr == true && state != 1) {
                    tokens.add("EXPR:" + expr);
                    expr = "";
                    isExpr = false;
                    tok = "";
                } else {
                    isExpr = true; // set is an expression to true
                    expr += tok; // append the operator
                    tok = ""; // reset token
                }

            } else if ("\"".equals(tok) || tok.equals(" \"")) { // if tok founds adouble quote
                if (state == 0) { // if the state is zero
                    state = 1; // set it to 1 meaning found a String
                    

                } else if (state == 1 && tok.endsWith(" \"")) {
                    tokens.add("STRING:" + string + " \""); // append the string into the string
                    state = 0; // reset values
                    string = ""; // reset values 
                    tok = ""; // reset valuss
// if it ends with space queote
                } else if (state == 1) { // if the state is 1 
                    tokens.add("STRING:" + string + "\""); // append the string into the string
                    state = 0; // reset values
                    string = ""; // reset values 
                    tok = ""; // reset valuss

                }
            } else if (state == 1) { // if state is zero 
                string += tok; // append the tok into string
                tok = ""; // reset tok 

            }
        }
 // System.out.println("Working Before returning Tokens: " + tokens);

        return tokens;

        // long duration = (System.nanoTime() - start)/ 1000;        
        //System.out.println(duration);
    }

    private static void parse(ArrayList toks) throws Exception {
        int i = 0;
        String value = "";
        String nextString = " ";
        String msg = "";
        String comparableOne = "";
        String comparableTwo = "";
        int j = 0;
        String exprVar = "";
        while (i < toks.size()) { // run until it checks the whole size
            String currentToken = toks.get(i).toString();
            if((toks.size() > i + 5) && currentToken.startsWith("IF") 
                    && (toks.get(i + 1).toString().startsWith("NUM")
                    || toks.get(i + 1).toString().startsWith("VAR")
                    ||(toks.get(i + 1).toString().startsWith("STRING")))
                    && toks.get(i + 2).toString().equals("EQEQ") // thjis can be added in the uture with other stuff
                    &&((toks.get(i + 3).toString().startsWith("NUM")
                    || toks.get(i + 3).toString().startsWith("VAR")
                    || toks.get(i + 3).toString().startsWith("STRING")))
                    && toks.get(i + 4).toString().startsWith("THEN")) { // if it follows the format of IF compare EQEQ compare THEN 
    

                if (toks.get(i + 1).toString().startsWith("VAR")) { // if the first compareable is a var
                    if(!getValueFromSymbol(toks.get(i + 1).toString().substring(4)).startsWith("Unidentified")) {
                       comparableOne = getValueFromSymbol(toks.get(i + 1).toString().substring(4));
                    }
                }
                if (toks.get(i + 3).toString().startsWith("VAR")) { // if the first compareable is a var
                    if(!getValueFromSymbol(toks.get(i + 1).toString().substring(4)).startsWith("Unidentified")) {
                       comparableTwo = getValueFromSymbol(toks.get(i + 3).toString().substring(4));
                    }
                }
                if (toks.get(i + 1).toString().startsWith("NUM")) { // if the first compareable is a var
                    comparableOne = toks.get(i + 1).toString().substring(4);
                }
                if (toks.get(i + 3).toString().startsWith("NUM")) { // if the first compareable is a var
                    comparableTwo = toks.get(i + 3).toString().substring(4);
                }
                if (toks.get(i + 1).toString().startsWith("STRING")) { // if the first compareable is a var
                    comparableOne = extractString(toks.get(i + 1).toString());
                }
                if (toks.get(i + 3).toString().startsWith("STRING")) { // if the first compareable is a var
                    comparableTwo = extractString(toks.get(i + 3).toString());
                }
                if(comparableOne.equals(comparableTwo)) {
                    
                    // if its true continue
                }else {
                    while(i < toks.size() && !toks.get(i).toString().equals("ENDIF")) { // skip until it fins an endif
                        i++;
                    }
                }
                lineNumber++;

            }
            else if ((i + 2 < toks.size()) && currentToken.equals("INPUT") && toks.get(i + 1).toString().startsWith("STRING") && toks.get(i + 2).toString().startsWith("VAR")) { // if it an input
                
                doAssign(toks.get(i + 2).toString().substring(4), getInput((toks.get(i + 1).toString()))); // calls the getInput method with the variable name and String msg as parameters

            } else if ((i + 2 < toks.size()) && currentToken.startsWith("VAR") && toks.get(i + 1).toString().equals("EQUALS")
                    && toks.get(i + 2).toString().startsWith("STRING") || (i + 2 < toks.size()) && currentToken.startsWith("VAR") && toks.get(i + 1).toString().equals("EQUALS")
                    && toks.get(i + 2).toString().startsWith("NUM") || (i + 2 < toks.size()) && currentToken.startsWith("VAR") && toks.get(i + 1).toString().equals("EQUALS")
                    && toks.get(i + 2).toString().startsWith("EXPR") || (i + 2 < toks.size()) && currentToken.startsWith("VAR") && toks.get(i + 1).toString().equals("EQUALS")
                    && toks.get(i + 2).toString().startsWith("VAR")) { // if its var and equals and string / num / expression
                if (toks.get(i + 2).toString().startsWith("STRING")) {
                    doAssign(currentToken.substring(4), extractString(toks.get(i + 2).toString())); // assign the variable
                    lineNumber++;
                } else if (toks.get(i + 2).toString().startsWith("NUM")) {
                    doAssign(currentToken.substring(4), toks.get(i + 2).toString().substring(4)); // assign the variable
                    lineNumber++;

                } else if (toks.get(i + 2).toString().startsWith("EXPR")) { // if its expression 
                    if((i + 3 < toks.size()) && toks.get(i + 3).toString().startsWith("VAR") 
                            && (i + 4 < toks.size()) && !toks.get(i + 4).toString().startsWith("EQUALS")) { // if the next is a var
                        int x  = i + 2;
                        while((x < toks.size() && toks.get(x).toString().startsWith("VAR") ||  toks.get(x).toString().startsWith("EXPR") )) {
                            if(symbol.containsKey(toks.get(x).toString().substring(4))) { // if its a var
                                exprVar += getValueFromSymbol(toks.get(x).toString().substring(4)) ; // get the value and append
                            }else {
                                exprVar += toks.get(x).toString().substring(5); // append the expression
                            }
                            x++;
                        }
                        doAssign(currentToken.substring(4), evaluateExpression(exprVar)); // assign the value from the expression
                        exprVar = ""; // reset the expressions
                         // set the i to x
                        lineNumber++;
                    } else {
                        doAssign(currentToken.substring(4), evaluateExpression(toks.get(i + 2).toString().substring(5))); // assign the variable
                        lineNumber++;

                    }

                } else if (toks.get(i + 2).toString().startsWith("VAR")) { // if it starts as var
                    if ((i + 3 < toks.size()) && toks.get(i + 2).toString().startsWith("VAR")  // var equals variable
                            || (i + 3 < toks.size()) && toks.get(i + 3).toString().equals("VAR") && !toks.get(i + 2).equals("EQUALS")) {
                        int x  = i + 2;
                        while((x < toks.size() && toks.get(x).toString().startsWith("VAR") ||  toks.get(x).toString().startsWith("EXPR") )) { // if started from var  or startrs with expr
                            if(symbol.containsKey(toks.get(x).toString().substring(4))) {
                                exprVar += getValueFromSymbol(toks.get(x).toString().substring(4)); // get the value and add to expr
                            }else {
                                exprVar += toks.get(x).toString().substring(5); // if its an expr append
                            }
                            x++;
                        }
                        doAssign(currentToken.substring(4), evaluateExpression(exprVar)); // evaluate the assign the value
                        exprVar = "";
                        lineNumber++;

                    } else if (!getValueFromSymbol(toks.get(i + 2).toString().substring(4)).startsWith("Unidentified")) { // if the varNamehas value
                        doAssign(toks.get(i).toString().substring(4), getValueFromSymbol(toks.get(i + 2).toString().substring(4))); // assign the variable
                        lineNumber++;

                    } else {
                        throw new Exception("Unidentified or Null Variable!: " + currentToken.substring(4));  // throw error if not
                    }

                }

            } else if (currentToken.startsWith("PRINT") && (i + 2 < toks.size()) || (i + 1 < toks.size()) && currentToken.startsWith("PRINT")) { // if its a print
                String nextToken = toks.get(i + 1).toString();
                if ((i + 3 < toks.size()) && nextToken.startsWith("NUM") && toks.get(i + 3).toString().startsWith("STRING") 
                        || (i + 3 < toks.size()) && nextToken.startsWith("STRING") && toks.get(i + 3).toString().startsWith("NUM") 
                        || (i + 3 < toks.size()) && (nextToken.startsWith("STRING") && !toks.get(i + 2).equals("INPUT"))
                        || (i + 3 < toks.size()) && nextToken.startsWith("STRING") && toks.get(i + 3).toString().startsWith("VAR")
                        || (i + 3 < toks.size()) && nextToken.startsWith("VAR") && toks.get(i + 3).toString().startsWith("STRING")
                        || (i + 3 < toks.size()) && nextToken.startsWith("VAR") && toks.get(i + 3).toString().startsWith("VAR")
                        || (i + 3 < toks.size()) && nextToken.startsWith("STRING") && toks.get(i + 3).toString().startsWith("STRING")) { // will check if its print for a String and if th next is not NPUt ot f its  anumber
                    j = i + 2;
                    value = extractString(nextToken);
                    if (symbol.containsKey(value)) {
                        value = getValueFromSymbol(extractString(nextToken)); // cleans the string
                    }
                    while ((j < toks.size()) && toks.get(j).equals("CONCAT")) { // runs until the size of the toks is greater and theres a concat
                        if(j + 2 >  toks.size()){ // to prevent out of bounds
                           break; // break
                        }
                        if (symbol.containsKey(toks.get(j + 1).toString().substring(4))) { // if its a var
                            nextString = getValueFromSymbol(toks.get(j + 1).toString().substring(4)); // append the value 

                        } else {
                            nextString = extractString(toks.get(j + 1).toString()); // appens the cleaned string

                        }
                        value += nextString; // combine the string
                        j += 2; // skip to the next concat


                    }
                    if(value.startsWith("STRING")) {
                        value = extractString(nextToken);
                    }
                    doPrint(value, Constants.STRING); // calls doPrint for String
                    i = j - 1;
                    lineNumber++;

                }else if(nextToken.startsWith("STRING")){
                  doPrint(extractString(toks.get(i + 1).toString()), Constants.STRING); // calls doPrint for STRING
                  lineNumber++;
                } else if (nextToken.startsWith("VAR")) { // will check if a print for variable
                    if (i + 2 < toks.size() && toks.get(i + 2).toString().startsWith("EXPR")) { // if an operator
                                
                        if (!getValueFromSymbol(nextToken.substring(4)).startsWith("Unidentified")) {
                            String expr = getValueFromSymbol(nextToken.substring(4)) + toks.get(i + 2).toString().substring(5);
                            doPrint(evaluateExpression(expr), Constants.STRING);
                            lineNumber++;

                        }
                    } else {
                        doPrint(toks.get(i + 1).toString().substring(4), Constants.VARIABLE);
                        lineNumber++;
                    }
                } else if (i + 1 < toks.size() && nextToken.startsWith("NUM")) {// will check if its a print for NUMBER {
                    doPrint(toks.get(i + 1).toString(), Constants.NUMBER); // calls doPrint for Numbers
                    lineNumber++;

                } else if (i + 1 < toks.size() && nextToken.startsWith("EXPR")) { // will check if its a print for expressions
                    doPrint(toks.get(i + 1).toString(), Constants.EXPRESSION); // calse the print for Expressions
                    lineNumber++;

                }
            }
            i++;
            
        }

    }
    
    
    private static String getInput(String msg) { //takes the input
        System.out.print(extractString(msg)); // prints the String
        String input = scan.nextLine(); // takes the actual input

        return input; // return the input
    }

    private static String getValueFromSymbol(String varName) throws Exception {
      
        if (symbol.containsKey(varName)) { // if variable exists
            return symbol.get(varName); // return the variable
        } else {
            return "Unidentified or Null Variable!: " + varName; // throws an error if it dosent exists
        }

    }

    private static String extractString(String token) { // removs quouotet and cleans the string
        if (token.startsWith("STRING")) { // if its starts with a string
            int start = token.indexOf("\"");
            int end = token.lastIndexOf("\""); // gets the last quote
            return token.substring(start + 1, end); // return the substring
        }else if(token.startsWith("VAR")) { // if its a var
                return token.substring(4);
        } else { // if number 
            return token.substring(4); // return 4 onwards
        }

    }

    private static void doAssign(String varName, String varValue) throws Exception { // stores the value of the variables nto the hashmap
        if (symbol.containsKey(varValue) && !symbol.containsKey(varName)) { // if symbol has varName and dosent have VarValue
            symbol.put(varName, symbol.get(varValue));  // append the varValue as a variable with varName's value
        } else if (!symbol.containsKey(varName)) { // if not currently in the synbo
            symbol.put(varName, varValue); // add to the hashmap

        } else if (symbol.containsKey(varName)) { // updates the value
            symbol.put(varName, varValue); // add to the hashmap

        }
        else { // if symbol dosent have variable
            throw new Exception("Unidentified or Null Variable!: " + varName);
        } // append the name and value
    }

    private static void doPrint(String toPrint, int type) throws Exception { // will handle the printing
        // System.out.println("Working DoPrint");
        if (type == Constants.STRING) { // checks if its a string literal 
            toPrint = toPrint;
        } else if (type == Constants.NUMBER) { // checks if its just a number
            toPrint = toPrint.substring(4); // starts at 4 onwards
        } else if (type == Constants.EXPRESSION) { // checks if its an expression
            toPrint = evaluateExpression(toPrint.substring(5)); // starts 5 onwards and evaluate the expression
        } else if (type == Constants.VARIABLE) { // checks if its an expression
            String temp = toPrint;
            toPrint = getValueFromSymbol(toPrint); // starts 5 onwards and evaluate the expression
            if (toPrint == null) { // if the variable dosent exists
                System.out.println("Undefined Variable!: " + temp); // log the error
            }
        }
        System.out.println(toPrint); // does the print for all if else
    }

    private static String caculateExpression(Queue outputQueue) throws Exception {
        Stack<String> exprStack = new Stack<>(); // will store the final result
        String result = "";
        while (!outputQueue.isEmpty()) { // iterate through all the Queue

            String token = outputQueue.poll().toString();

            if (UtilsMath.isNumeric(token)) { //  is a number
                exprStack.push(token); // push the token if its a number
            } else if (!exprStack.isEmpty() && token.matches("[-/*+]+")) { // if the stack is not empty and an operator
                int right = Integer.parseInt(exprStack.pop()); // get the number from stack
                int left = Integer.parseInt(exprStack.pop()); // get the number from stack

                if ("*".equals(token)) { // if its a multiplication
                    result = String.valueOf(left * right);  // multiply it left to right
                } else if ("/".equals(token)) {
                    if (right != 0) { // if its not division by zero
                        result = String.valueOf(left / right); // divide left to right
                    } else {
                        throw new Exception("Division by zero is not allowed"); // throws arithmetic exception
                    }
                } else if ("+".equals(token)) { // if its an addition

                    result = String.valueOf(left + right); // left to right add
                } else if ("-".equals(token)) { // if its subtraction
                    result = String.valueOf(left - right);  // subtract left to right
                }
                exprStack.push(result); // push the result

            }

        }
        return result;
    }

    // SHUNTING YARD ALGORITHM
    private static String evaluateExpression(String expr) throws Exception { // evaluate the math expression
        Queue<String> outputQueue = new LinkedList<>(); // will hold the final outputs for math expressions
        Stack<Character> operatorStack = new Stack<>(); // will store the operators for math expressions
        String string = ""; // used for building whole numbers
        for (int i = 0; i < expr.length(); i++) { // for loop for all expr 
            //    System.out.println(operatorStack);
            if (Character.isDigit(expr.charAt(i))) { // if its number push to output
                string += expr.charAt(i); // concat with the string
                if (i == expr.length() - 1 || !Character.isDigit(expr.charAt(i + 1))) { // if the next index is not a number                   
                    outputQueue.add(string); // push the whole number in
                    string = ""; // reset the string
                }

            } else if (expr.charAt(i) == '(') { // if i is an open paren
                if (i != 0 && (UtilsMath.isNumeric(outputQueue.peek()) || expr.charAt(i - 1) == ')')) {

                    /* 
                    if i is not equals to zero just for safety
                    and if the character before it is a number
                    or another close paren
                    add multiplication
                     */
                    operatorStack.push('*'); // push the multplication

                }
                operatorStack.push(expr.charAt(i)); // add the open paren token
            } // for else if empty
            else if (expr.charAt(i) == ')') { // if its a close paren

                while (!operatorStack.isEmpty() && !(operatorStack.peek() != ('('))) {
                    /*
                    while th operator stack is not empty
                    and while the operator stack is not equals to open paren
                     */
                    outputQueue.add(operatorStack.pop().toString()); // push the operators inthe final QUEUE
                }
                if (!operatorStack.isEmpty()) { // if stack is not empty
                    operatorStack.pop(); // pop the open paren

                }
            } else if (expr.charAt(i) == '+' /* if its an operator */
                    || expr.charAt(i) == '-'
                    || expr.charAt(i) == '*'
                    || expr.charAt(i) == '/') {
             
                if (expr.charAt(i) == '-' && i == 0 || Character.toString(expr.charAt(i - 1)).matches("[-/*+]+")
                        || (i > 0) && expr.charAt(i) == '-' && expr.charAt(i - 1) == '(') {

                    string += expr.charAt(i); // append the current Character
                }  else {
                    while (!operatorStack.isEmpty() // while the operator stack is not empty
                            && !operatorStack.peek().equals('(') // and not equals to the open paren
                            && UtilsMath.precedenceLevel(operatorStack.peek().toString().charAt(0)) // and the top operator is has much
                            >= UtilsMath.precedenceLevel(expr.charAt(i))) { // higher precedence to the new operator

                        outputQueue.add(operatorStack.pop().toString());  //add the top operator to the QUEUE
                    }
                    operatorStack.push(expr.charAt(i)); // push the new operator to the stack

                } // for the else if opeator
            }

        } // for for loop
       
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek() != null) {
                outputQueue.add(operatorStack.pop().toString());
            } else {
                break;
            }
        }
        System.out.println(outputQueue);
        expr = caculateExpression(outputQueue); // will call the calculate method
        return expr;
    }

    private static void runPrompt() {

    }

}
