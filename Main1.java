import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main1 {

    // Путь к текстовому файлу с заданиями (ссылка и путь сохранения)
    private static final String TASK_FILE = "src/links.txt";

    public static void main(String[] args) {
        // 1. ЧТЕНИЕ ФАЙЛА С ЗАДАНИЯМИ
        List<String> lines;
        try {
            // Читаем все строки из файла (используем UTF-8, чтобы русские буквы читались нормально)
            lines = Files.readAllLines(Paths.get(TASK_FILE), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Ошибка: Не удалось прочитать файл " + TASK_FILE);
            e.printStackTrace();
            return; // Завершаем программу, если файл не найден
        }

        // Проверяем, что в файле есть хотя бы 2 строки (для картинки и для аудио)
        if (lines.size() < 2) {
            System.err.println("Ошибка: В файле должно быть 2 строки (ссылка и путь для картинки, затем для mp3)");
            return;
        }

        // 2. РАЗБОР СТРОК (Формат: "https://... C:/путь/файл.jpg")
        // Разделяем строку по первому пробелу, чтобы отделить ссылку от пути
        String[] imageData = lines.get(0).split(" ", 2);
        String[] audioData = lines.get(1).split(" ", 2);

        if (imageData.length < 2 || audioData.length < 2) {
            System.err.println("Ошибка: Неверный формат строк. Пример: <ссылка> <путь>");
            return;
        }

        String imageUrl = imageData[0];
        String imagePath = imageData[1].trim();

        String audioUrl = audioData[0];
        String audioPath = audioData[1].trim();

        // 3. СКАЧИВАНИЕ В ДВА ПОТОКА (Параллельно)
        // Массив для хранения статуса загрузки: [0] - картинка, [1] - аудио
        boolean[] downloadSuccess = new boolean[2];

        // Создаем поток для скачивания картинки
        Thread imageThread = new Thread(() -> {
            downloadSuccess[0] = downloadFile(imageUrl, imagePath, "Картинка");
        });

        // Создаем поток для скачивания mp3
        Thread audioThread = new Thread(() -> {
            downloadSuccess[1] = downloadFile(audioUrl, audioPath, "MP3");
        });

        // Запускаем оба потока одновременно
        imageThread.start();
        audioThread.start();

        try {
            // Ждем, пока оба потока закончат работу
            imageThread.join();
            audioThread.join();
        } catch (InterruptedException e) {
            System.err.println("Ожидание скачивания было прервано.");
            return;
        }

        // 4. ВОСПРОИЗВЕДЕНИЕ
        // Если mp3 скачался успешно, запускаем плеер
        if (downloadSuccess[1]) {
            playMp3(audioPath);
        } else {
            System.err.println("Не удалось скачать mp3, воспроизведение отменено.");
        }

        // 5. ОБФУСКАЦИЯ КОДА
        System.out.println("\n=== ЗАПУСК ОБФУСКАЦИИ ИСХОДНОГО КОДА ===");
        obfuscateJavaFile("src/Main.java"); // Обфусцируем этот же файл
    }

    /**
     * Метод для скачивания файла по URL.
     */
    private static boolean downloadFile(String urlString, String savePath, String fileType) {
        System.out.println(fileType + ": Начинаю скачивание...");
        long startTime = System.currentTimeMillis();

        try {
            File file = new File(savePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            URLConnection connection = new URL(urlString).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println(fileType + ": Успешно скачано за " + (endTime - startTime) + " мс -> " + savePath);
            return true;

        } catch (IOException e) {
            System.err.println(fileType + ": ОШИБКА СКАЧИВАНИЯ (" + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Метод для воспроизведения MP3 файла.
     */
    private static void playMp3(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Файл не найден: " + filePath);
            return;
        }

        System.out.println("Начинаю воспроизведение: " + filePath);
        try (FileInputStream fileInput = new FileInputStream(file);
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInput)) {

            Player player = new Player(bufferedInput);
            player.play();
            player.close();

            System.out.println("Воспроизведение завершено.");

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        } catch (JavaLayerException e) {
            System.err.println("Ошибка декодирования MP3: " + e.getMessage());
        }
    }

    // МЕТОД ДЛЯ ОБФУСКАЦИИ

    private static void obfuscateJavaFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Файл для обфускации не найден: " + filePath);
            return;
        }

        try {
            // 1. Читаем исходный код
            String code = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            // 2. Удаляем комментарии
            code = code.replaceAll("//.*", "");
            code = code.replaceAll("/\\*.*?\\*/", "");

            // 3. Сжимаем код (удаляем лишние пробелы и переносы)
            code = code.replaceAll("\\s+", " ");
            code = code.replaceAll("\\s*([{}();,=+\\-*/<>!&|?:])\\s*", "$1");

            // 4. Меняем имя класса Main на A
            code = code.replaceAll("\\bMain\\b", "A");

            // 5. Заменяем идентификаторы на a, b, c...
            List<String> keywords = Arrays.asList(
                    "public", "private", "static", "void", "int", "String", "return", "if", "else",
                    "new", "class", "final", "double", "boolean", "import", "package", "try", "catch", "while", "for", "throws", "extends", "implements", "this"
            );

            // Разбиваем текст на слова
            String[] words = code.split("[^a-zA-Z0-9_]+");
            Set<String> variables = new HashSet<>();

            for (String word : words) {
                if (word.length() > 0
                        && !keywords.contains(word)
                        && !Character.isDigit(word.charAt(0))
                        && !word.equals("A")) {
                    variables.add(word);
                }
            }

            // Создаем словарь замен
            char newChar = 'a';
            for (String var : variables) {
                // Игнорируем стандартные классы, чтобы не сломать импорты
                if (var.equals("Thread") || var.equals("IOException") || var.equals("File")
                        || var.equals("Player") || var.equals("Files") || var.equals("Paths")
                        || var.equals("StandardCharsets") || var.equals("System") || var.equals("String")) {
                    continue;
                }

                code = code.replaceAll("\\b" + var + "\\b", String.valueOf(newChar));
                newChar++;
            }

            // 6. Сохраняем результат в файл A.java
            File outputFile = new File(file.getParent(), "A.java");
            Files.write(outputFile.toPath(), code.getBytes(StandardCharsets.UTF_8));

            System.out.println("Обфускация завершена! Файл сохранен: " + outputFile.getAbsolutePath());
            System.out.println("\n--- Полученный код ---");
            System.out.println(code);

        } catch (IOException e) {
            System.err.println("Ошибка при обфускации: " + e.getMessage());
        }
    }
}