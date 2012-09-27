package TwoJFrames;

import java.awt.Component;

import javax.swing.Icon;

public class Tab {
	private String title, toolTipText;
	private Component component, tabComponent;
	private Icon icon;			
	private boolean enabled;
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public void setComponent(Component component) {
		this.component = component;
	}
	
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setTabComponent(Component tabComponent) {
		this.tabComponent = tabComponent;
	}
}
