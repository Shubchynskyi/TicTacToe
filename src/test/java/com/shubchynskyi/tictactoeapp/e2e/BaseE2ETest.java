package com.shubchynskyi.tictactoeapp.e2e;

import com.shubchynskyi.tictactoeapp.TicTacToeApplication;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = {TicTacToeApplication.class}
)
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    @Value("${local.server.port:8080}")
    protected int port;

    @Value("${e2e.headlessIsOn:false}")
    protected boolean headlessIsOn;

    protected WebDriver driver;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver(getChromeOptions());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        if (headlessIsOn) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-features=SameSiteByDefaultCookies");
        }
        return options;
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }
}