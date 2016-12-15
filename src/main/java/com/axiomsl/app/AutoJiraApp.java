package com.axiomsl.app;

import java.io.IOException;
import java.net.URISyntaxException;

import com.axiomsl.api.app.AbstractApp;
import com.axiomsl.api.app.AppPreloader;
import com.axiomsl.view.AutoJiraController;
import com.sun.javafx.application.LauncherImpl;


@SuppressWarnings("restriction")
public class AutoJiraApp extends AbstractApp {

		@Override
		public String initTitle() {
			
			return "Prime/New Dev Release Notes";
			
		}

		@Override
		public void initControllers() {

			addChild(loadController(AutoJiraController.class.getResource("AutoJiraApp.fxml")));
			
		}

		@Override
		 protected void showPrimaryStage() {
			
		  setWidth(500.0);
		  setHeight(575.0);
		  super.showPrimaryStage();
		  
		 }

	
		public static void main(String[] args) throws URISyntaxException, IOException {
			
			LauncherImpl.launchApplication(AutoJiraApp.class, AppPreloader.class, args);
			
		}

}
