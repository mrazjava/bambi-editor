package org.zimowski.bambi.editor.plugins.api;

import java.awt.Component;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.zimowski.bambi.editor.config.ConfigLoader;

/**
 * Base for image export plugins. Since any export process could be time  
 * consuming, the work must be done off the EDT so that GUI remains responsive.  
 * Plugins deriving from this base must override {@link #doInBackground()} with  
 * the actual export logic along with proper notifications of monitor members.  
 * The {@link #done()} is already implemented as it informs monitors that the   
 * export has finished, but deriving class might want to override it (ensuring  
 * to call base version first!), if cleanup or additional post-processing is 
 * desired. All parameters from {@link #export(ImageExportDef)}, which is 
 * fully implemented and need not be overriden, are exposed within the 
 * protected member with an identical name.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractImageExporter extends AbstractImagePlugin implements ImageExporter {
	
	/**
	 * Property key for tooltip
	 */
	public static final String CONFIG_TOOTLIP = "tooltip";
	
	/**
	 * Upload definition objects
	 */
	protected ImageExportDef exportDef;
	
	/**
	 * UI parent component; may be null if not provided
	 */
	protected Component parent;

	
	@Override
	public void export(ImageExportDef uploadDef, Component parent) {
		
		this.exportDef = uploadDef;
		this.parent = parent;
		execute();
	}

	@Override
	protected void done() {
		Date doneTime = new Date();
		List<ExportStateMonitor> monitors = exportDef.getStateMonitors();
		for(ExportStateMonitor monitor : monitors) 
			monitor.exportFinished(doneTime);
	}
	
	/**
	 * {@inheritDoc} The tooltip is looked under configuration key in the 
	 * format of XXX.ISO where XXX is {@link #CONFIG_TOOTLIP}, dot is   
	 * {@link ConfigLoader#PARAM_SEPARATOR}, and ISO is {@link Locale#getISO3Language()}. 
	 * If locale specific value is not found, a second search is done just 
	 * for {@link #CONFIG_TOOTLIP}. If that yields no result, null is 
	 * returned.
	 */
	@Override
	public String getTooltip(Locale locale) {
		String tooltipKey = "tooltip";
		String tooltip = configuration.getProperty(tooltipKey + ConfigLoader.PARAM_SEPARATOR + locale.getISO3Language());
		if(StringUtils.isEmpty(tooltip)) {
			tooltip = configuration.getProperty(tooltipKey);
		}
		return tooltip;
	}
}