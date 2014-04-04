package org.jenkinsci.plugins.categorizedview;
import hudson.model.BallColor;
import hudson.model.HealthReport;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.search.SearchIndex;
import hudson.search.Search;
import hudson.security.ACL;
import hudson.security.Permission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jenkins.model.Jenkins;

import org.acegisecurity.AccessDeniedException;
import org.joda.time.DateTime;

public class GroupTopLevelItem  implements TopLevelItem{
	private final String groupName;

	public TopLevelItem target;
	private int nestLevel;
	private final String groupClass;
	protected List<TopLevelItem> nestedItems = new ArrayList<TopLevelItem>();
	
	public GroupTopLevelItem(String groupLabel) {
		groupName = groupLabel;
		this.nestLevel = 0;
		this.groupClass = "g_"+groupLabel.replaceAll("[^a-zA-Z0-9_]","_")+groupLabel.hashCode();		
		this.specificCss.append("font-weight:bold;");
	}

	public String getName() {
		return groupName;
	}

	public String getFullName() {
		return groupName;
	}

	public String getDisplayName() {
		return groupName;
	}

	public String getFullDisplayName() {
		return groupName;
	}

	public void add(TopLevelItem item) {
		nestedItems.add(item);
	}

	public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
	}

	public void onCopiedFrom(Item src) {
	}

	public void onCreatedFromScratch() {
	}

	public void save() throws IOException {
	}

	public void delete() throws IOException, InterruptedException {
	}

	public void checkPermission(Permission permission) throws AccessDeniedException {
	}
	
	public BallColor getIconColor() {
		BallColor colorState = BallColor.NOTBUILT;
		for (TopLevelItem items : getNestedItems()) {
			if (items instanceof AbstractProject) {
				BallColor projectColorState = ((AbstractProject)items).getIconColor();
				colorState = chooseNextColor(colorState, projectColorState);
			}
		}
		return colorState;
	}
	
	public Run getLastBuild() {
		return getLastBuildOfType(new GetBuild() {
			public AbstractBuild getFrom(AbstractProject project) {
				return project.getLastBuild();
			}
		});
    }
	
	public Run getLastSuccessfulBuild() {
		return getLastBuildOfType(new GetBuild() {
			public AbstractBuild getFrom(AbstractProject project) {
				return (AbstractBuild) project.getLastSuccessfulBuild();
			}
		});
	}
	
	public Run getLastStableBuild() {
		return getLastBuildOfType(new GetBuild() {
			public AbstractBuild getFrom(AbstractProject project) {
				return (AbstractBuild) project.getLastStableBuild();
			}
		});
	}

	public Run getLastFailedBuild() {
		return getLastBuildOfType(new GetBuild() {
			public AbstractBuild getFrom(AbstractProject project) {
				return (AbstractBuild) project.getLastFailedBuild();
			}
		});
	}
	
	public Run getLastUnsuccessfulBuild() {
		return getLastBuildOfType(new GetBuild() {
			public AbstractBuild getFrom(AbstractProject project) {
				return (AbstractBuild) project.getLastUnsuccessfulBuild();
			}
		});
	}

	public Run getLastBuildOfType(GetBuild getBuild) {
		AbstractBuild lastBuild = null;
		for (TopLevelItem items : getNestedItems()) {
			if (items instanceof AbstractProject) {
				AbstractBuild build = getBuild.getFrom((AbstractProject)items);
				if (lastBuild == null)
					lastBuild = build;
				else {
					if (new DateTime(build.getTimestamp()).isAfter(new DateTime(lastBuild.getTimestamp()))) {
						lastBuild = build;
					}
				}
			}
		}
		return lastBuild;
	}
	
	static interface GetBuild {
		public AbstractBuild getFrom(AbstractProject project);
	}

	public BallColor chooseNextColor(BallColor res, BallColor iconColor) {
		switch(res) {
		case ABORTED: case ABORTED_ANIME:
			switch(iconColor) {
			case YELLOW: case YELLOW_ANIME:
			case RED: case RED_ANIME:
				res = iconColor;
				break;
			default:
			}
			break;
		case BLUE: case BLUE_ANIME:
			switch(iconColor) {
			case ABORTED: case ABORTED_ANIME:
			case YELLOW: case YELLOW_ANIME:
			case RED: case RED_ANIME:
				res = iconColor;
				break;
			default:
			}
			break;
		case YELLOW: case YELLOW_ANIME:
			switch(iconColor) {
			case RED: case RED_ANIME:
				res = iconColor;
				break;
			default:
			}
			break;
		case RED: case RED_ANIME:
			break;
		default:
			res = iconColor;
		}
		return res;
	}

	public String getUrl() {
		return "";
	}

	public String getShortUrl() {
		return "";
	}

	@Deprecated
	public String getAbsoluteUrl() {
		return null;
	}

	public File getRootDir() {
		return null;
	}

	public Search getSearch() {
		return null;
	}

	public String getSearchName() {
		return "";
	}

	public String getSearchUrl() {
		return "";
	}

	public SearchIndex getSearchIndex() {
		return null;
	}

	public ACL getACL() {
		return null;
	}

	public boolean hasPermission(Permission permission) {
		return true;
	}

	public Jenkins getParent() {
		return Jenkins.getInstance();
	}

	public TopLevelItemDescriptor getDescriptor() {
		return null;
	}

	public HealthReport getBuildHealth() {
		HealthReport lowest = new HealthReport();
		lowest.setScore(100);
		for (TopLevelItem e : getNestedItems()) {
			if (e instanceof AbstractProject) {
				HealthReport buildHealth = ((AbstractProject)e).getBuildHealth();
				if (buildHealth.getScore() < lowest.getScore())
					lowest = buildHealth;
			}
		}
		return lowest;
	}

	public List<HealthReport> getBuildHealthReports() {
		return null;
	}

	public boolean isBuildable() {
		return false;
	}


	public String getRelativeNameFrom(ItemGroup g) {
		return getName();
	}

	public String getRelativeNameFrom(Item item) {
		return getName();
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends Job> getAllJobs() {
		return Collections.EMPTY_LIST;
	}

	public List<TopLevelItem> getGroupItems() {
		return getNestedItems();
	}

	public int getNestLevel() {
		return nestLevel;
	}
	
	public boolean hasLink() {
		if (target == null) return false;
		return target.getShortUrl() != null;
	}
	
	public String getGroupClass() {
		return groupClass;
	}

	public String getCss() {
		StringBuilder builder = getBasicCss();
		return builder.toString();
	}

	private StringBuilder getBasicCss() {
		StringBuilder builder = new StringBuilder();
		builder.append(specificCss.toString());
		return builder;
	}

	StringBuilder specificCss = new StringBuilder();
	
	public List<TopLevelItem> getNestedItems() {
		final Comparator<TopLevelItem> comparator = new TopLevelItemComparator();
		Collections.sort(nestedItems,comparator);
		return nestedItems;
	}
}
