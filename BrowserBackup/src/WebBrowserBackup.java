import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class WebBrowserBackup {
    private static WebDriver driver;

    private static final String CONST_backupTypeGoogleKeep = "googleKeep";
    private static final String CONST_backupTypeZenMoney = "zenMoney";

    private static final String[] CONST_availableBackupTypes = {CONST_backupTypeGoogleKeep, CONST_backupTypeZenMoney};

    // процедура ожидания
    private static void wait(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("waiting finished");
    }

    // всякие необходимые инициализационные штуки для драйвера
    private static void initResources(String webBrowserProfile, Boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {
        System.setProperty("webdriver.gecko.driver", "dependencies\\geckodriver.exe"); // укаываем местоположение драйверов для firefox
        // System.setProperty("webdriver.chrome.driver", "C:\googleKeepBackup\dependencies\\chromedriver.exe"); // chrome не исполльзуется, потому что с ним были ошибки. Но пусть будет на всякий случай для истории

        // этот профиль мы загружаем для того, чтобы вместе с ним подхватывались данные аутентификации в cookies.
        // В этом профиле вбиты данные аутентификации Google и Dropbox.
        // Профиль настраивается вручную через firefox.
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile firefoxProfile = profile.getProfile( webBrowserProfile );

        FirefoxOptions options = new FirefoxOptions();

        options.setProfile(firefoxProfile);
        options.setHeadless(useWebBrowserWithoutGuiInHiddenHeadlessMode);
        System.out.println("starting selenium web driver");

        driver = new FirefoxDriver(options);
    }

    // типа деструктора ресурсов. Закрываем все соединения, закрываем браузер и т.д.
    private static void closeResources() {
        driver.close(); // maybe just call driver.quit() and not driver.close() ? Selenium Chrome extension generated only call for  driver.quit() and did not mention driver.close()
                        // maybe removing driver.close() will fix the bug with some exception in my typical selenium log?
        driver.quit();
    }


    private static void runGoogleKeepBackup() {

        // собственно переход на интересующую страница Google, где можно сделать бэкап
        driver.get("https://takeout.google.com/settings/takeout");

        // кликаем по кнопке "Отменить выбор", чтобы снялся выбор со всех сервисов. Мне нужен экспорт только одного сервиса.
        WebElement composeBtn = driver.findElement(By.xpath("//*[@aria-label='Отменить выбор']")); // есть - думаю, что это более-менее универсальная завязка
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(1); // здесь ожидания используются для того, что страница успевала прогружаться.

        // выбираем сервис импорта Google Keep
        // composeBtn = driver.findElement(By.xpath("//*[@aria-label='Выбрать: Keep']"));
        composeBtn = driver.findElement(By.xpath("//*[@aria-label='Выбрать: Google Keep']"));
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(1);


        composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Далее')]")); // если хочешь завязаться именно на div, то нужно выбирать самый глубокий div, который содержит текст. Не хочу с этим разбираться, поэтому пока так
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(5);

        /* выбираем область в которой можно через клики по TAB добраться до метода получения архива. Это любая область.
        Поэтому выбираем тело HTML. Важно, чтобы элемент был interactable! В противном случае получим ошибку ElementNotInteractableException */

        WebElement clickElement = driver.findElement(By.xpath("//body"));
        clickElement.sendKeys(Keys.TAB); // перешли на способ
        wait(1);
        clickElement.sendKeys(Keys.ENTER); // клацаем по элементу кнопкой и получаем раскрытый список методов получения.
        wait(1);

        // здесь выбор дропбокса
        clickElement.sendKeys(Keys.DOWN); // после выполнения этой команды будет выделена опция "Добавить на Диск" (до этого была опция "По ссылке").
        wait(1);

        clickElement.sendKeys(Keys.DOWN); // после выполнения этой команды будет выделена опция "Добавить в Dropbox"
        wait(1);

        clickElement.sendKeys(Keys.ENTER); // клацаем по пункту меню "Добавить в Dropbox"
        wait(1);

        // жмем наконец кнопку бэкапа
        composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Связать аккаунты и создать экспорт')]"));
        executor.executeScript("arguments[0].click();", composeBtn);


        wait(3);


// это работает, но нужно будет закомментировать на текущий момент, чтобы у меня был тесткейз, где нужно вводить пароль
        // нажимаем кнопку "Далее" после ввода пароля
        composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Далее')]"));
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(1);


        System.out.println("script finished");
        wait(600); // подождем 10 минут или 600 секунд. Т.е. в течении этого времени окно браузера firefox будет октрыто. Это время нужно для того, чтобы увидеть, на чем застрял бэкап в случае проблем.

    }

    private static void runZenMoneyBackup() {
        String targetUrl = "https://zenmoney.ru/a/#export";

        // собственно переход на интересующую страницу ZenMoney с авторизацией от Google
        driver.get(targetUrl); // заходим на эту страницу, чтобы не было странных вызовов API facebook, yandex и т.д., которые есть на главной странице

        // клик по авторизации Google
        WebElement composeBtn = driver.findElement(By.id("proceedGoogle")); // TODO: запилить метод нажимания на кнопку pressButton(element)
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(10); // ждем, пока произойдет авторизация от гугла
        System.out.println("google auth executed");

        driver.get(targetUrl); // еще раз заходим на страницу экспорта, потому что слетает ссылка в корень после авторизации от Google

        wait(10); // ждем, пока форма прогрузит страницу экспорта. Там джаваскрипт быстро не прогружает, поэтому сразу элементы не появляются. -- TODO: можно использовать WebDriverWait.wait.until вместо кастомного ожидания

        driver.findElement(By.xpath("//*[@id='import']/form")).submit();

        System.out.println("script finished");
        wait(600); // подождем 10 минут или 600 секунд. Т.е. в течении этого времени окно браузера firefox будет октрыто.

    }

    private static void redirectAllOutputToFile(String outputFile) {

        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);
        System.setErr(out);

    }

    private static void runBackup(String backupType, String webBrowserProfile, Boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {

        Boolean inputBackupTypeValid = false;
        for (String availableBackupItem: CONST_availableBackupTypes) {
            if(availableBackupItem.equals(backupType)) {
                inputBackupTypeValid = true;
                break;
            }
        }

        if(inputBackupTypeValid) {
            runBackup_int(backupType, webBrowserProfile, useWebBrowserWithoutGuiInHiddenHeadlessMode);
        }
        else {
            System.out.println("'" + backupType + "' is not available backup type. List of available backup types: " + getPrintableAvailableBackupTypes() );
        }

    }

    // todo: мне кажется, что лучше не передавать параметры в процедурах, а сделать эти параметры полями класса (по идее, статическими? ). Даже, возможно, стоит сделать какой-нибудь класс BackupParameters
    private static void runBackup_int(String backupType, String webBrowserProfile, Boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {

        long unixTime = System.currentTimeMillis() / 1000L;
        String outputFile = "log\\" + backupType+ "Backup" + webBrowserProfile +  unixTime + ".log"; // формируем лог файлы, ничего не перезаписывая, чтобы можно было отслелить происходящее с течением времени
        redirectAllOutputToFile(outputFile);

        initResources(webBrowserProfile, useWebBrowserWithoutGuiInHiddenHeadlessMode);

        /*
         todo: этот код - потенциальный кандидат на разделение классов. Т.е. на каждый тип бэкапа можно иметь свой подкласс. Можно создать суперкласс, который содержит метод runBackup
        * тогда этот код оперировал бы терминами суперкласса. Т.е. безусловно вызывал бы метод runBackup. А конкрктный подкласс, который бы присваивался переменной с типом суперкласса, определялся бы в фабричном методе.
        */
        switch (backupType) {
            case CONST_backupTypeGoogleKeep:
                runGoogleKeepBackup();
                break;
            case CONST_backupTypeZenMoney:
                runZenMoneyBackup();
                break;
            default:
                System.out.println("Invalid or not yet implemented backup Type. Please extend this procedure if you need one more backup type");
                break;
        }

        closeResources();
    }


    private static String getPrintableAvailableBackupTypes() {

        String printableAvailableBackupTypes = "";
        for (String availableBackupItem: CONST_availableBackupTypes) {
            printableAvailableBackupTypes += "'" + availableBackupItem + "'; "; // TODO: fix IDEA warnings. Сейчас не критично.
        }
        return printableAvailableBackupTypes;
    }

    private static String getHelp() {
         return "Utility for backup in browser. Utility uses selenium web driver to do backups from different services available for real users only. \n" +
                "Usage: javaw -jar WebBrowserBackupProgramJarFile backupType webBrowserProfile \n" +
                "All parameters are required. \n" +
                "backupType available values: " + getPrintableAvailableBackupTypes() + " \n" +
                "webBrowserProfile: navigate to your browser and get its profile name here. Now Firefox only supported. Profile is used to set your auth information for sites where backup runs \n" +
                "If you want help, then put only -help parameter"
                ;
    }

    private static void printHelp() {
        System.out.println( getHelp() );
    }

    public static void main( String[] args )
    {
        /*
         TODO: refactor this. Параметры передаются неудобно. Нужно использовать какой-нибудь стандартный фреймворк передачи параметров из разряда "key-value", чтобы можно было параметры передавать в произвольном порядке
        * например, так: javaw -jar WebBrowserBackupProgramJarFile -backupType=zenMoney ..."
        * иначе клиенту нужно помнить позиционно все параметры, что не очень хорошо. К тому же клиенту сейчас нужно заполнять все параметры. А если бы была структура "key-value", то некоторые параметры можно было бы сделать опциоальными.
        */
        if( args.length == 0 || // если на вход получили пустую строку параметров
            args[0].equals("-help") || // или явно пользователь просит помощь
            args.length != 3 // или количество входных параметров не равно допустимому. На текущий момент допустимо только 2 параметра. Не больше, не больше.
           ) {
            printHelp();
        }
        else { // предполагаем, что злесь пользователь уже понимает, что нужно делать.
            // CONST_backupTypeZenMoney
            // CONST_backupTypeGoogleKeep
            String backupType = args[0];
            String webBrowserProfile = args[1];
            Boolean doNotUseWebBrowserWithoutGuiInHiddenHeadlessMode = ( args[2].equals( "useWebBrowserWithoutGuiInHiddenHeadlessMode=false") );

            runBackup( backupType, webBrowserProfile, !doNotUseWebBrowserWithoutGuiInHiddenHeadlessMode );
        }

    }
}