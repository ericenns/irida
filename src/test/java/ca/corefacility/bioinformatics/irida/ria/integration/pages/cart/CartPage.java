package ca.corefacility.bioinformatics.irida.ria.integration.pages.cart;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import ca.corefacility.bioinformatics.irida.ria.integration.pages.AbstractPage;

public class CartPage extends AbstractPage {
	@FindBy(className = "t-cart-empty")
	private WebElement emptyCartElement;

	@FindBy(className = "t-cart-sample")
	private List<WebElement> cartSamples;

	@FindBy(className = "t-pipelines")
	private WebElement pipelinesView;

	public CartPage(WebDriver driver) {
		super(driver);
	}

	public static CartPage goToCart(WebDriver driver) {
		get(driver, "/cart/pipelines");
		return PageFactory.initElements(driver, CartPage.class);
	}

	public boolean isEmptyCartDisplayed() {
		return emptyCartElement.isDisplayed();
	}

	public int getNumberOfSamplesInCart() {
		return driver.findElements(By.className("t-cart-sample")).size();
	}

	public boolean onPipelinesView() {
		return pipelinesView.isDisplayed();
	}

	public void removeSampleFromCart(int index) {
		WebElement sample = cartSamples.get(index);
		sample.findElement(By.className("t-delete-menu-btn")).click();
		WebElement deleteMenu = driver.findElement(By.className("t-delete-menu"));
		deleteMenu.findElement(By.className("t-delete-sample")).click();
		// Need to wait for the sample to be removed from the UI.
		waitForTime(500);
	}

	public void removeProjectFromCart() {
		WebElement sample = cartSamples.get(0);
		sample.findElement(By.className("t-delete-menu-btn"))
				.click();
		WebElement deleteMenu = driver.findElement(By.className("t-delete-menu"));
		deleteMenu.findElement(By.className("t-delete-project")).click();
		waitForTime(500);
	}

	public void selectPhylogenomicsPipeline() {
		goToPipelinePage("t-SNVPhyl_Phylogenomics_Pipeline_btn");
	}

	public void selectAssemblyPipeline() {
		goToPipelinePage("t-Assembly_and_Annotation_Pipeline_btn");
	}

	private void goToPipelinePage(String pipeline) {
		get(driver, "cart/pipelines");
		WebElement btn = waitForElementVisible(By.className(pipeline));
		btn.click();
	}
}