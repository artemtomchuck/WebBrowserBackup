import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class WebBrowserBackup {
    private static WebDriver driver;

    private static final String CONST_backupTypeGoogleCalendar = "GoogleCalendar";
    private static final String CONST_backupTypeGoogleChrome = "GoogleChrome";
    private static final String CONST_backupTypeGoogleContacts = "GoogleContacts";
    private static final String CONST_backupTypeGoogleKeep = "GoogleKeep";
    private static final String CONST_backupTypeGoogleMail = "GoogleMail";
    private static final String CONST_backupTypeGoogleMusicAndYoutube = "GoogleMusicAndYoutube";
    private static final String CONST_backupTypeGoogleReminders = "GoogleReminders";
    private static final String CONST_backupTypeGoogleTasks = "GoogleTasks";


    private static final String[] CONST_availableBackupTypes = {CONST_backupTypeGoogleCalendar, CONST_backupTypeGoogleChrome, CONST_backupTypeGoogleContacts, CONST_backupTypeGoogleKeep, CONST_backupTypeGoogleMail, CONST_backupTypeGoogleMusicAndYoutube, CONST_backupTypeGoogleReminders, CONST_backupTypeGoogleTasks };

    // TODO: it is better to implement waiting procedure as a part Selenium driver (I have seen some examples in Internet like WebDriverWait.wait.until or something similar).
    //  Maybe this approach will lead to necessity to write custom "until" condition in each case
    // TODO: improve logging whenever this wait is called. It would be nice to know at which step this wait is executed
    private static void wait(int seconds) {
        try {
            Thread.sleep(seconds* 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("waiting finished");
    }

    // required initializations for driver, profiles etc
    private static void initResources(String webBrowserProfile, boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {
        System.setProperty("webdriver.gecko.driver", "dependencies\\geckodriver.exe"); // path for driver which controls the Firefox
        // System.setProperty("webdriver.chrome.driver", "dependencies\\chromedriver.exe"); // Chrome is not used here because there were some issues with HeadMode in Chrome. But leave this line as an example

        // we use this profile in order to load authentication data which is stored in cookies.
        // In this profile you should be logged into Google and Dropbox accounts.
        // You should setup this Firefox profile manually (one-time action during initial application setup)
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile firefoxProfile = profile.getProfile( webBrowserProfile );

        FirefoxOptions options = new FirefoxOptions();

        options.setProfile(firefoxProfile);
        options.setHeadless(useWebBrowserWithoutGuiInHiddenHeadlessMode);
        System.out.println("starting selenium web driver");

        driver = new FirefoxDriver(options);
    }

    // (kind of) destructor of resources. Close driver, close browser etc
    private static void closeResources() {
        driver.quit();
    }

    private static String getServiceNameOnGoogleTakeoutPage(String backupType) {
        // sort Google services alphabetically
        switch (backupType) {
            case CONST_backupTypeGoogleCalendar:
                return "Календарь";
            case CONST_backupTypeGoogleChrome:
                return "Chrome";
            case CONST_backupTypeGoogleContacts:
                return "Контакты";
            case CONST_backupTypeGoogleKeep:
                return "Google Keep";
            case CONST_backupTypeGoogleMail:
                return "Почта";
            case CONST_backupTypeGoogleMusicAndYoutube:
                return "YouTube и YouTube Music";
            case CONST_backupTypeGoogleReminders:
                return "Напоминания";
            case CONST_backupTypeGoogleTasks:
                return "Задачи";
            default:
                System.out.println("Invalid or not yet implemented backup Type. Please extend this procedure if you need one more backup type");
                return null;
        }
    }

    private static void handleGoogleAuthenticationRequest() {
        String currentUrl = driver.getCurrentUrl();

        // if Google requests us to provide password then Firefox will substitute this password automatically and we we will just click "Next" button
        // if Google does not request password (can happen sometimes if you execute backup few times in the same browser session) then previous step had to already start the backup process and noting to do anymore
        if(currentUrl.startsWith("https://accounts.google.com")) {
            WebElement composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Далее')]"));
            JavascriptExecutor executor = (JavascriptExecutor)driver;
            executor.executeScript("arguments[0].click();", composeBtn);

            wait(10);
        }
    }

    // this function can execute the list of provided Google backups in single GoogleTakeout archive
    // But you don't necessarily need to execute multiple type of backups - just provide array with one element in such case
    private static void runGoogleTakeoutBackup(String[] backupTypeList) {

        final String googleTakeoutUrl = "https://takeout.google.com/settings/takeout";

        // go to Google Takeout page where you can trigger backup execution
        driver.get(googleTakeoutUrl);

        // I am not sure 100%, but Google may request authentication here
        handleGoogleAuthenticationRequest();

        // if previous open of Google Takeout page requested authentication then open Google Takeout page again.
        // This time Google probably shouldn't request authentication anymore
        driver.get(googleTakeoutUrl);

        // click the button in order to deselect all services. Selection of every service is default choice on Google Takeout page
        // But in the scope of this backup we need to choose only single service
        WebElement composeBtn = driver.findElement(By.xpath("//*[@aria-label='Отменить выбор']"));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(1); // wait in order to make sure that page is loaded properly before going to next action

        for (String backupType : backupTypeList) {
            String serviceNameOnGoogleTakeoutPage = getServiceNameOnGoogleTakeoutPage(backupType);

            // choose Google service which data we want to export
            composeBtn = driver.findElement(By.xpath("//*[@aria-label='Выбрать: " + serviceNameOnGoogleTakeoutPage + "']"));
            executor.executeScript("arguments[0].click();", composeBtn);
            wait(2);
        }

        composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Далее')]"));
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(5);

        // choose some area in HTML which you can use as a starting point for clicking keyboard buttons.
        // This can by any area which is interactable. If not interactable then you will get ElementNotInteractableException
        // So we choose HTML body as a starting point.
        // Clicking TAB button in order to choose appropriate option from menu
        WebElement clickElement = driver.findElement(By.xpath("//body"));
        clickElement.sendKeys(Keys.TAB); // after this we are on menu of method for retrieving data
        wait(1);
        clickElement.sendKeys(Keys.ENTER); // enter into this menu by pressing Enter (drop-down list with data retrieving methods will be opened)
        wait(1);

        // the next step is to choose Dropbox as method for retrieving data

        clickElement.sendKeys(Keys.DOWN); // after this command the option with Google Drive will be highlighted (before this command option "Download using link" was highlighted)
        wait(1);

        clickElement.sendKeys(Keys.DOWN); // after this command the option with Dropbox will be highlighted
        wait(1);

        clickElement.sendKeys(Keys.ENTER); // choose Dropbox option by pressing Enter
        wait(1);

        // click backup button
        composeBtn = driver.findElement(By.xpath("//span[contains(string(), 'Связать аккаунты и создать экспорт')]"));
        executor.executeScript("arguments[0].click();", composeBtn);

        wait(5);

        // typically Google request authentication in this place
        handleGoogleAuthenticationRequest();
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

    private static void runBackup(String backupTypeCsvList, String webBrowserProfile, boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {
        String[] requestedBackupTypeList = backupTypeCsvList.split(",");

        boolean isRequestedBackupTypeValid = false;

        for (String requestedBackupType: requestedBackupTypeList) {
            isRequestedBackupTypeValid = false;

            for (String availableBackupType: CONST_availableBackupTypes) {
                if(availableBackupType.equals(requestedBackupType)) {
                    isRequestedBackupTypeValid = true;
                    break;
                }
            }

            if(!isRequestedBackupTypeValid) {
                System.out.println("'" + requestedBackupType + "' is not available backup type. List of available backup types: " + getPrintableAvailableBackupTypes() );
                return;
            }
        }

        runSeleniumBackup(requestedBackupTypeList, webBrowserProfile, useWebBrowserWithoutGuiInHiddenHeadlessMode);
    }

    // TODO: it seems that it is better to avoid passing parameters in procedures. You can define these parameters as class members (probably static members). Maybe, it is even better to create some class BackupParameters
    private static void runSeleniumBackup(String[] backupTypeList, String webBrowserProfile, boolean useWebBrowserWithoutGuiInHiddenHeadlessMode) {
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // each call will generate separate log file with timestamp for better debugging capabilities.
        // Sometimes you may want to understand how your script worked one month ago and timestamps will be handy in such case
        String outputFile = "log\\" + formattedDateTime + "_backup.log";

        redirectAllOutputToFile(outputFile);

        System.out.println("Log parameters: ");
        System.out.println("webBrowserProfile = " + webBrowserProfile);
        System.out.println("backupTypeList = " + Arrays.toString(backupTypeList));

        initResources(webBrowserProfile, useWebBrowserWithoutGuiInHiddenHeadlessMode);

        // TODO: this code is candidate for class splitting. There can be superclass which has runBackup() method. And each backup_type can have its own subclass.
        //  You can define the specific subclass in fabric method so runBackup() will use only abstract operations and all specifics will be handled in fabric methods and subclasses

        // currently only Google backup is supported here. If there will be other backup type then you should split the calls:
        // So that runGoogleTakeoutBackup is called only with Google type backups input. All other backup types should be handled in separate call
        runGoogleTakeoutBackup( backupTypeList );

        System.out.println("backup finished");

        wait(15);

        closeResources();
    }

    private static String getPrintableAvailableBackupTypes() {
        return Arrays.toString(CONST_availableBackupTypes);
    }

    private static String getHelp() {
         return "Utility for backup in browser. Utility uses selenium web driver to do backups from different services available for real users only. \n" +
                "Usage: javaw -jar WebBrowserBackupProgramJarFile backupTypeList webBrowserProfile useWebBrowserWithoutGuiInHiddenHeadlessMode=[true, false] \n" +
                "All parameters are required. \n" +
                "backupTypeList available values can be provided as comma ',' separated: " + getPrintableAvailableBackupTypes() + " \n" +
                "webBrowserProfile: navigate to your browser and get its profile name here. Now Firefox only supported. Profile is used to set your auth information for sites where backup runs \n" +
                "useWebBrowserWithoutGuiInHiddenHeadlessMode: if you don't want to see browser window during backup then set it to true. Default value of this parameter is true\n" +
                "If you want to see this help, then put only -help parameter"
                ;
    }

    private static void printHelp() {
        System.out.println( getHelp() );
    }

    public static void main( String[] args )
    {
        // TODO: refactor this. It is not handy parameter passing. Consider using some standard framework for OS-parameter passing (kind of "key-value")
        //  so user will be able to pass parameter in any order and any not populated parameters will be set to default values.
        //  Currently the order of parameters matters and all parameters should be provided which is inconvenient. Example call with desired parameters handling
        //  javaw -jar WebBrowserBackupProgramJarFile -backupType=GoogleCalendar -webBrowserProfile=myProfile ...
        //
        if( args.length == 0 || // if we have empty parameter string as input
            args[0].equals("-help") || // or user explicitly asks for help
            args.length != 3 // or number of parameters is unexpected. Currently we have strictly 3 parameters
           ) {
            printHelp(); // TODO: if there is something wrong with parameters then just say that something is wrong. Do not just show help.
        }
        else { // assumption here that user already understands what he does
            String backupTypeCsvList = args[0];
            String webBrowserProfile = args[1];
            boolean doNotUseWebBrowserWithoutGuiInHiddenHeadlessMode = ( args[2].equals( "useWebBrowserWithoutGuiInHiddenHeadlessMode=false") );

            runBackup( backupTypeCsvList, webBrowserProfile, !doNotUseWebBrowserWithoutGuiInHiddenHeadlessMode );
        }
    }
}