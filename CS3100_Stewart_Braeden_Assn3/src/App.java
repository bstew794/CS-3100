import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private ArrayList<String> cmdHistory = new ArrayList<>();
    private Path currentDir;
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private double waitTime = 0;

    public static void main(String[] args){
        Boolean repeat = true;
        Boolean repeatCMD = false;
        String console = "bash";
        String shellC = "-c";
        String[] osNameArray = System.getProperty("os.name").toLowerCase().split("\\s+");
        String osName = osNameArray[0];
        int oldCMDIndex = -1;

        if (osName.equals("windows")){
            console = "cmd.exe";
            shellC = "/c";
        }
        App application = new App();

        while(repeat){
            ArrayList<String> argsList;
            String argsStr;

            if (!repeatCMD){
                application.setCurrentDir();
                Scanner scanner = new Scanner(System.in);

                System.out.print("[" + application.currentDir.toString() + "]: ");
                argsStr = scanner.nextLine();

                String[] arguments = splitCommand(argsStr);
                application.cmdHistory.add(argsStr);
                argsList = new ArrayList<>(Arrays.asList(arguments));
            }
            else{
                argsStr = application.cmdHistory.get(oldCMDIndex);
                String[] arguments = splitCommand(argsStr);
                argsList = new ArrayList<>(Arrays.asList(arguments));

                repeatCMD = false;
            }
            if (argsList.isEmpty()){
                application.help();
                continue;
            }
            if (argsList.contains("|")){
                application.pipe(console, shellC, argsList);
                continue;
            }
            String command = argsList.get(0);
            argsList.remove(0);

            if (command.equals("exit")){
                System.exit(0);
            }
            else if (command.equals("mdir")){
                application.mdir(argsList);
            }
            else if (command.equals("rdir")){
                application.rdir(argsList);
            }
            else if (command.equals("history")){
                application.listHistory();
            }
            else if (command.equals("^")){
                if (argsList.isEmpty()){
                    System.out.println("not enough parameters provided");
                    application.help();
                }
                else{
                    int newIndex = application.useOldCMD(argsList.get(0));
                    if (newIndex >= 0){
                        repeatCMD = true;
                        oldCMDIndex = newIndex;
                    }
                }
            }
            else if (command.equals("list")){
                application.listFiles();
            }
            else  if (command.equals("cd")){
                application.cd(argsList);
            }
            else if (command.equals("ptime")){
                application.pTime();
            }
            else{
                argsList.add(0, command);
                System.out.println(application.consoleCMDS(console, shellC, argsList));
            }
        }
    }
    /**
     * Split the user command by spaces, but preserving them when inside double-quotes.
     * Code Adapted from: https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
     */
    private static String[] splitCommand(String command) {
        java.util.List<String> matchList = new java.util.ArrayList<>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(command);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList.toArray(new String[matchList.size()]);
    }
    private void setCurrentDir(){

        currentDir = Paths.get(System.getProperty("user.dir"));
    }
    private void help(){
        System.out.println("The command you entered is not a valid one, look at the examples below: \n");
        System.out.println("mdir [DIRECTORY TO BE CREATED]");
        System.out.println("rdir [DIRECTORY TO BE REMOVED]");
        System.out.println("history");
        System.out.println("^ [INDEX OF COMMAND BEING ACCESSED IN COMMAND HISTORY]");
        System.out.println("list");
        System.out.println("cd [DIRECTORY PATH]");
        System.out.println("ptime");
        System.out.println("[EXTERNAL COMMAND] [EXTERNAL PARAMETERS]");
        System.out.println("[EXTERNAL COMMAND] [EXTERNAL PARAMETERS] | [EXTERNAL COMMAND] [EXTERNAL PARAMETERS]");
        System.out.println("exit");
        System.out.println();
    }
    private void pTime(){
        System.out.print("Time spent waiting on child processes: ");
        System.out.printf("%.4f", waitTime);
        System.out.println();
    }
    private String consoleCMDS(String console, String shellC, ArrayList<String> parameters){
        if (parameters.isEmpty()){
            return "insufficient Parameters were given";
        }
        boolean wait = true;
        String endStr = parameters.get(parameters.size() - 1);

        if (endStr.equals("&")){
            wait = false;
            parameters.remove(parameters.size() - 1);
        }
        String paraStr = "";

        for (int i = 0; i < parameters.size(); i++){
            if (i == 0){
                paraStr += parameters.get(i);
            }
            else{
                paraStr += " " + parameters.get(i);
            }
        }
        try{
            processBuilder.command(console, shellC, paraStr + " " + currentDir.toString());
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null){
                output.append(line + "\n");
            }
            if (wait){
                double startTime = System.currentTimeMillis();
                int exitVal = process.waitFor();
                if (exitVal == 0){
                    double endTime = System.currentTimeMillis();
                    double deltaTime = endTime - startTime;
                    waitTime += (deltaTime / 1000);
                    return output.toString();
                }
                else{
                    double endTime = System.currentTimeMillis();
                    double deltaTime = endTime - startTime;
                    waitTime += (deltaTime / 1000);
                    return "That command is invalid";
                }
            }
            else{
                return output.toString();
            }
        }
        catch (Exception e){
            return e.toString();
        }
    }
    private void pipe(String console, String shellC, ArrayList<String> parameters){
        ArrayList<String> firstList = new ArrayList<>();
        ArrayList<String> endList = new ArrayList<>();
        Boolean addFirst = true;

        for (String parameter : parameters){
            if (parameter.equals("|")){
                if (addFirst){
                    addFirst = false;
                }
                else{
                    addFirst = true;
                }
            }
            else{
                if (addFirst){
                    firstList.add(parameter);
                }
                else{
                    endList.add(parameter);
                }
            }
        }
        System.out.println(consoleCMDS(console, shellC, firstList));
        System.out.println(consoleCMDS(console, shellC, endList));

    }
    private void mdir(ArrayList<String> dirNames){
        if (dirNames.get(dirNames.size() - 1).equals("&")){
            dirNames.remove(dirNames.size() - 1);
        }
        if (dirNames.isEmpty()){
            System.out.println("not enough parameters provided");
            help();
        }
        else{
            for (String dirName : dirNames){
                Path relative = Paths.get(dirName);
                Path absolute = relative.toAbsolutePath();
                String dirPath = absolute.toString();
                if (new File(dirPath).mkdirs()){
                    System.out.println("directory was successfully created");
                }
                else{
                    System.out.println("Error: directory already exists");
                }
            }
        }
    }
    private void rdir(ArrayList<String> dirNames){
        if (dirNames.get(dirNames.size() - 1).equals("&")){
            dirNames.remove(dirNames.size() - 1);
        }
        if (dirNames.isEmpty()){
            System.out.println("not enough parameters provided");
            help();
        }
        else{
            for (String dirName : dirNames){
                Path relative = Paths.get(dirName);
                Path absolute = relative.toAbsolutePath();
                String dirPath = absolute.toString();
                File dir = new File(dirPath);

                if (dir.exists()){
                    if (deleteDir(dir)){
                        System.out.println("directory successfully deleted");
                    }
                    else{
                        System.out.println("Error: unable to delete directory");
                    }
                }
                else{
                    System.out.println("Error: directory does not exist");
                }
            }
        }
    }
    private Boolean deleteDir(File directory){
        if (directory.isDirectory()){
            File[] children = directory.listFiles();

            for (File child : children){
                if (!deleteDir(child)){
                    return false;
                }
            }
        }
        return directory.delete();
    }
    private void listHistory(){
        for (int i = 0; i < cmdHistory.size(); i++){
            String message = "Command " + (i + 1) + ": " + cmdHistory.get(i);
            System.out.println(message);
        }
    }
    private int useOldCMD(String parameter){
        try{
            int cmdIndex = Integer.parseInt(parameter) - 1;
            if (cmdIndex >= cmdHistory.size() - 1){
                System.out.println("Error: index is out of bounds");
                return -1;
            }
            else{
                return cmdIndex;
            }
        }
        catch(NumberFormatException e){
            System.out.println("Error: argument provided is not a valid integer");
            return -1;
        }
    }
    private void listFiles(){
        File dir = new File(currentDir.toString());
        File[] children = dir.listFiles();

        if (children.length <= 0){
            System.out.println("There are no files in this directory");
        }
        else{
            String isDir = "d";
            String isRead = "r";
            String isWrite = "w";
            String isExecute = "x";

            for (File child: children){
                String typeStr = "";

                if (child.isDirectory()){
                    typeStr += isDir;
                }
                if (child.canRead()){
                    typeStr += isRead;
                }
                if (child.canWrite()){
                    typeStr += isWrite;
                }
                if (child.canExecute()){
                    typeStr += isExecute;
                }
                long numOfBytes = child.length();
                String byteStr = Long.toString(numOfBytes);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

                long dateModified = child.lastModified();
                String dateString = dateFormat.format(dateModified);

                String childName =  child.getName();

                System.out.printf( "%-4s %10s %s %s %n", typeStr, byteStr, dateString, childName);
            }
            System.out.println();
        }
    }
    private void cd(ArrayList<String> dirNames){
        if (dirNames.isEmpty()){
            String home = System.getProperty("user.home");
            System.setProperty("user.dir", home);
        }
        else if (dirNames.get(dirNames.size() - 1).equals("&")){
            dirNames.remove(dirNames.size() - 1);
        }
        else{
            for (String dirName : dirNames){
                if (dirName.equals("..")){
                    String dirPath = currentDir.getParent().toString();
                    System.setProperty("user.dir", dirPath);
                    continue;
                }
                Path absolute = Paths.get(currentDir.toString(), dirName);
                String dirPath = absolute.toString();
                File dir = new File(dirPath);

                if (dir.exists()){
                    System.setProperty("user.dir", dirPath);
                }
                else{
                    System.out.println("Error: The specified pathway could not be found");
                }
            }
        }
    }
}



