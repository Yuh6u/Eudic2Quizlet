import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;

public class Parser {
    private static final String DIR_PATH = "/Users/bytedance/Documents/";
    private static final String OUTPUT_FILE = "output.txt";

    public static File getFiles() {
        File file = new File(DIR_PATH);
        File[] files = file.listFiles();
        if (files == null) {
            System.err.println("Could not find directory path: " + DIR_PATH);
            return null;
        }
        System.out.println("Directory found");
        File targetFile = null;
        for (File f : files){
            if (f.getName().startsWith("我的学习记录_")) {
                targetFile = f;
                break;
            }
        }
        if (targetFile == null){
            System.err.println("Could not found target file");
            return null;
        }
        System.out.println("Target file found");
        return targetFile;
    }

    public static void processFile(File file, boolean enableWriteFile, boolean enableClipboard) {
        if (file == null)
            return;
        int lineNum = 0;
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = enableWriteFile ? new BufferedWriter(new FileWriter(DIR_PATH + OUTPUT_FILE)) : null) {
            StringBuilder builder = new StringBuilder();
            String[] contents = null;
            int wordsCount = 0;
            // the line format is that:
            // 序号，单词，音标，解释，笔记
            for (; (line = reader.readLine()) != null; lineNum++) {
                // skip first line since it is annotation
                if (lineNum == 0 || line.isEmpty() || line.isBlank())
                    continue;
                // if starts with number, means this is a new word
                if (isNumber(line.charAt(0))) {
                    if (wordsCount != 0)
                        builder.append("\n");
                    wordsCount++;
                    contents = line.split(",");
                    builder.append(contents[1]).append('/').append(contents[3]);
                } else if ('-' == line.charAt(0)) {
                    builder.append(',').append(line.substring(1));
                }
            }
            String result = builder.toString().replaceAll("\"", "");
            if (enableClipboard)
                setSysClipboardText(result);
            if (enableWriteFile)
                writer.write(result);
            System.out.println("Process file finished, " + wordsCount + " words recorded");
        } catch (Throwable throwable) {
            System.err.println("Error happens during process file at line " + lineNum);
            System.err.println("Content: " + line);
            System.err.println("Error message:" + throwable.getMessage());
        }
    }

    private static boolean isNumber (char c){
        return '0' <= c && '9' >= c;
    }

    private static void setSysClipboardText(String writeMe) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = new StringSelection(writeMe);
        clipboard.setContents(content, null);
    }

    public static void main(String[] args) {
        if (args.length == 0){
            processFile(getFiles(), true, true);
            return;
        }
        if (args[0].equalsIgnoreCase("-w")){
            processFile(getFiles(), true, false);
            return;
        }
        if (args[0].equalsIgnoreCase("-c")){
            processFile(getFiles(), false, true);
        }
    }
}
