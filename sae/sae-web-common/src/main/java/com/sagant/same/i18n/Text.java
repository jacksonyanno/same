/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.
 *
 * SAME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sagant.same.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.common.SAEProfile;

public class Text extends ResourceBundle {

	protected static final String BUNDLE_NAME = Text.class.getCanonicalName().toLowerCase();
	protected static final String BUNDLE_EXTENSION = "properties";
	protected static final Control UTF8_CONTROL = new UTF8Control();
	
	protected static final String PROPERTY_KEY_I18N_BASE_REST_URL = "com.sagant.same.i18n.provider.base_rest_url";
	
	public Text() {
		setParent(ResourceBundle.getBundle(BUNDLE_NAME, FacesContext
				.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
	}

	@Override
	protected Object handleGetObject(String key) {
		return parent.getObject(key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getKeys() {
		return parent.getKeys();
	}

	protected static class UTF8Control extends Control {
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException,
				IOException {
			// The below code is copied from default Control#newBundle()
			// implementation.
			// Only the PropertyResourceBundle line is changed to read the file
			// as UTF-8.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, BUNDLE_EXTENSION);
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload) {
				URL url = loader.getResource(resourceName);
				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			} else {
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null) {
				try {
					bundle = new PropertyResourceBundle(new InputStreamReader(
							stream, "UTF-8"));
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}

	
	protected static class RestControl extends UTF8Control {
		
		protected String i18nBaseRestURL;
		protected String tenantid;
		
		protected Logger log = Logger.getLogger(RestControl.class);
		
		public RestControl() {
			super();

			i18nBaseRestURL = SAEProfile.getInstance().getProperties().getProperty(PROPERTY_KEY_I18N_BASE_REST_URL);
			tenantid = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("tenantid");
		}
		
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload) 
				throws IllegalAccessException, InstantiationException, IOException {

			//Check if we want to use i18n service provider or standard java resource bundle
			if (i18nBaseRestURL == null) {
				return super.newBundle(baseName, locale, format, loader, reload);
			}
			
			
			String bundleName = toBundleName(baseName, locale);
			String[] bundleNameSplited = bundleName.split(".");
			String fileName = bundleNameSplited[bundleNameSplited.length-2];
			String stringLocale = bundleNameSplited[bundleNameSplited.length-1];
			
			String i18nRestURL = i18nBaseRestURL+"/"+fileName+"."+BUNDLE_EXTENSION+"?lc="+stringLocale;
			
			//Si estoy en modo multitenant incluyo el tenantid, de lo contrario no
			if (tenantid != null) {
				i18nRestURL += "&tenantid="+tenantid; 
			}


			//Invoco al rest que me provee el archivo de properties localizado
			//TODO
			//String stringBundle = "";

			ResourceBundle bundle = null;
			InputStream stream = null;
			
			try {
			
				URL url = new URL(i18nRestURL);
				URLConnection connection = url.openConnection();
				connection.setUseCaches(false);
				
				stream = connection.getInputStream();

				bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
				
			} catch (MalformedURLException e) {
				log.error("[i18n] Malformed url("+i18nRestURL+") loaded from property ("+PROPERTY_KEY_I18N_BASE_REST_URL+")", e);
				log.error("[i18n] Switching to java resource bundle");
				return super.newBundle(baseName, locale, format, loader, reload);

			} catch (IOException e) {
				log.error("[i18n] Can not open connection to i18n service url("+i18nRestURL+")", e);
				log.error("[i18n] "+e.getMessage());
				log.error("[i18n] Switching to java resource bundle");
				return super.newBundle(baseName, locale, format, loader, reload);
				
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					log.error("[i18n] Can not close connection to i18n service url("+i18nRestURL+")", e);
				}
			}

			return bundle;
		}
	}

}