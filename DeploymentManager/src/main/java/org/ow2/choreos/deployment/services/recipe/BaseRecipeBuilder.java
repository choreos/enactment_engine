package org.ow2.choreos.deployment.services.recipe;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.Recipe;
import org.ow2.choreos.services.datamodel.RecipeBundle;

public abstract class BaseRecipeBuilder implements RecipeBuilder {

	private Logger logger = Logger.getLogger(BaseRecipeBuilder.class);

	private static final File DEST_DIR = new File(
			"src/main/resources/chef/recipes");

	private final String templateDir;
	private final String recipeName;
	private String recipeFile;

	public BaseRecipeBuilder(String templateDir, String recipeName) {
		this.templateDir = templateDir;
		this.recipeName = recipeName;
	}

	public RecipeBundle createServiceRecipeBundle(
			DeployableServiceSpec serviceSpec) {

		RecipeBundle recipeBundle = new RecipeBundle();
		Recipe serviceRecipe;
		Recipe activateRecipe;
		Recipe deactivateRecipe;

		try {
			recipeBundle.setCookbookFolder(DEST_DIR.getAbsolutePath());

			serviceRecipe = createServiceRecipe(serviceSpec);
			recipeBundle.setServiceRecipe(serviceRecipe);

			activateRecipe = createActivateRecipe(serviceSpec);
			recipeBundle.setActivateRecipe(activateRecipe);

			deactivateRecipe = createDeactivateRecipe(serviceSpec);
			recipeBundle.setDeactivateRecipe(deactivateRecipe);

			return recipeBundle;

		} catch (IOException e) {
			logger.error("Could not create recipe", e);
		}

		return null;

	}

	void changeAttributesDefaultRb(DeployableServiceSpec serviceSpec,
			String filename) throws IOException {
		changeFileContents(serviceSpec, filename);
	}

	void changeRecipeRb(DeployableServiceSpec serviceSpec, String filename)
			throws IOException {
		changeFileContents(serviceSpec, filename);
	}

	void changeMetadataRb(DeployableServiceSpec serviceSpec, String filename)
			throws IOException {
		changeFileContents(serviceSpec, filename);
	}

	private Recipe createDeactivateRecipe(DeployableServiceSpec serviceSpec)
			throws IOException {
		Recipe activateRecipe = new Recipe();
		activateRecipe.setName("activate-service" + this.recipeName);
		activateRecipe.setCookbookName("activate-service"
				+ serviceSpec.getUUID());
		this.recipeFile = "activate-" + this.recipeName + ".rb";

		File srcFolder = new File(this.templateDir
				+ "deactivate-service-template");

		String destPath = DEST_DIR.getAbsolutePath() + "/deactivate-service"
				+ serviceSpec.getUUID();

		File destFolder = new File(destPath);

		copyRecipeTemplate(srcFolder, destFolder);
		changeMetadataRb(serviceSpec,
				"/deactivate-service" + serviceSpec.getUUID() + "/metadata.rb");
		changeAttributesDefaultRb(serviceSpec, "/deactivate-service"
				+ serviceSpec.getUUID() + "/attributes/default.rb");
		changeRecipeRb(serviceSpec,
				"/deactivate-service" + serviceSpec.getUUID()
						+ "/recipes/default.rb");

		return activateRecipe;
	}

	private Recipe createActivateRecipe(DeployableServiceSpec serviceSpec)
			throws IOException {
		Recipe activateRecipe = new Recipe();
		activateRecipe.setName("activate-service" + this.recipeName);
		activateRecipe.setCookbookName("activate-service"
				+ serviceSpec.getUUID());
		this.recipeFile = "activate-" + this.recipeName + ".rb";

		File srcFolder = new File(this.templateDir
				+ "activate-service-template");

		String destPath = DEST_DIR.getAbsolutePath() + "/activate-service"
				+ serviceSpec.getUUID();

		File destFolder = new File(destPath);

		copyRecipeTemplate(srcFolder, destFolder);
		changeAttributesDefaultRb(serviceSpec, "/activate-service"
				+ serviceSpec.getUUID() + "/attributes/default.rb");

		return activateRecipe;
	}

	private Recipe createServiceRecipe(DeployableServiceSpec serviceSpec)
			throws IOException {
		Recipe serviceRecipe = new Recipe();
		serviceRecipe.setName(this.recipeName);
		serviceRecipe.setCookbookName("service" + serviceSpec.getUUID());
		this.recipeFile = this.recipeName + ".rb";

		File srcFolder = new File(this.templateDir
				+ "service-deploy-recipe-template");

		String destPath = DEST_DIR.getAbsolutePath() + "/service"
				+ serviceSpec.getUUID();

		File destFolder = new File(destPath);

		copyRecipeTemplate(srcFolder, destFolder);
		changeMetadataRb(serviceSpec, "/service" + serviceSpec.getUUID()
				+ "/metadata.rb");
		changeAttributesDefaultRb(serviceSpec,
				"/service" + serviceSpec.getUUID() + "/attributes/default.rb");

		changeRecipeRb(serviceSpec, "/service" + serviceSpec.getUUID()
				+ "/recipes/" + this.recipeFile);

		return serviceRecipe;
	}

	private void changeFileContents(DeployableServiceSpec serviceSpec,
			String fileLocation) throws IOException {

		File file = new File(DEST_DIR + fileLocation);

		String fileData = null;
		synchronized (BaseRecipeBuilder.class) {
			fileData = FileUtils.readFileToString(file);
		}

		fileData = this.replace(fileData, serviceSpec);

		synchronized (BaseRecipeBuilder.class) {
			FileUtils.deleteQuietly(file);
			FileUtils.writeStringToFile(file, fileData);
		}
	}

	String copyRecipeTemplate(File srcFolder, File destFolder)
			throws IOException {

		synchronized (BaseRecipeBuilder.class) {
			try {
				FileUtils.copyDirectory(srcFolder, destFolder);
			} catch (IOException e) {
				logger.warn("IOException when copying recipe template; it should not happen");
				File dest = new File(destFolder.getAbsolutePath());
				if (!dest.exists())
					throw e;
			}
		}

		return destFolder.getAbsolutePath();
	}

	/**
	 * Replace content from cookbook files with information retrieved from
	 * service specification
	 * 
	 * @param content
	 * @param serviceSpec
	 * @return
	 */
	public abstract String replace(String content,
			DeployableServiceSpec serviceSpec);

}
