/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.tasks.groupings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dmfs.android.xmlmagic.AndroidParserContext;
import org.dmfs.android.xmlmagic.builder.RecyclingReflectionObjectBuilder;
import org.dmfs.android.xmlmagic.builder.ReflectionObjectBuilder;
import org.dmfs.xmlobjects.ElementDescriptor;
import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.builder.IObjectBuilder;
import org.dmfs.xmlobjects.builder.reflection.Attribute;
import org.dmfs.xmlobjects.builder.reflection.Element;
import org.dmfs.xmlobjects.pull.XmlObjectPull;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.dmfs.xmlobjects.pull.XmlPath;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;


/**
 * This class describes the tab configuration. It holds a number of tabs with attributes like title, icon and visibility.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TabConfig
{

	/**
	 * The XML namespace.
	 */
	public final static String NAMESPACE = "http://schema.dmfs.org/tasks";

	/**
	 * The name of the root elements.
	 */
	public final static String TAG = "tabconfig";

	/**
	 * A Builder that builds a {@link TabConfig} object.
	 */
	public final static IObjectBuilder<TabConfig> BUILDER = new RecyclingReflectionObjectBuilder<TabConfig>(TabConfig.class);

	/**
	 * The {@link XmlElementDescriptor} of the tabconfig element.
	 */
	public final static ElementDescriptor<TabConfig> DESCRIPTOR = ElementDescriptor.register(QualifiedName.get(NAMESPACE, TAG), BUILDER);

	/**
	 * A Builder for {@link Tab} objects.
	 */
	public final static IObjectBuilder<Tab> TAB_BUILDER = new ReflectionObjectBuilder<Tab>(Tab.class);

	/**
	 * The {@link XmlElementDescriptor} for tab elements.
	 */
	public final static ElementDescriptor<Tab> TAB_DESCRIPTOR = ElementDescriptor.register(QualifiedName.get(NAMESPACE, Tab.TAG), TAB_BUILDER);

	/**
	 * Represents a single tab with all its attributes.
	 * 
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	public static class Tab
	{
		public final static String TAG = "tab";

		@Attribute(name = "title")
		private int title;

		@Attribute(name = "icon")
		private int icon;

		@Attribute(name = "id")
		private int id;

		@Attribute(name = "visible")
		private boolean visible = true;


		/**
		 * Get the title of this tab.
		 * 
		 * @return A string resource id for the title.
		 */
		public int getTitleId()
		{
			return title;
		}


		/**
		 * Get the icon of the tab.
		 * 
		 * @return A drawable resource id.
		 */
		public int getIcon()
		{
			return icon;
		}


		/**
		 * Get the id of the tab.
		 * 
		 * @return The id.
		 */
		public int getId()
		{
			return id;
		}


		/**
		 * Return the visibility of the tab.
		 * 
		 * @return <code>true</code> if the tab is visible, <code>false</code> otherwise.
		 */
		public boolean isVisible()
		{
			return visible;
		}
	}

	/**
	 * All loaded tabs.
	 */
	@Element(namespace = NAMESPACE, name = Tab.TAG)
	private ArrayList<Tab> mTabs;

	/**
	 * The visible tabs.
	 */
	private List<Tab> mVisible;


	/**
	 * Loads a {@link TabConfig} from the given XML resource.
	 * <p>
	 * A tabconfig XML file must looke like this:
	 * </p>
	 * 
	 * <pre>
	 * &lt;tabconfig xmlns="http://schema.dmfs.org/tasks" >
	 * 
	 *     &lt;tab
	 *         id="@+id/tab1_id"
	 *         icon="@drawable/tab1_icon"
	 *         title="@string/tab1_title" />
	 *     &lt;tab
	 *         id="@+id/tab2_id"
	 *         icon="@drawable/tab2_icon"
	 *         title="@string/tab2_title"
	 *         visible="false"/>
	 * &lt;/tabconfig>
	 * </pre>
	 * 
	 * @param context
	 *            A {@link Context}.
	 * @param tabsResource
	 *            The resource id of an XML resource that contains the tabconfig.
	 * @return A {@link TabConfig} instance.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws XmlObjectPullParserException
	 */
	public static TabConfig load(Context context, int tabsResource) throws XmlPullParserException, IOException, XmlObjectPullParserException
	{
		Resources res = context.getResources();

		XmlResourceParser parser = res.getXml(tabsResource);

		XmlObjectPull objectParser = new XmlObjectPull(parser, new AndroidParserContext(context, null));

		TabConfig groupings = objectParser.pull(DESCRIPTOR, null, new XmlPath());
		groupings.updateVisible();

		return groupings;
	}


	/**
	 * Get the {@link Tab} at the specified position.
	 * 
	 * @param position
	 *            The position among all {@link Tab}s.
	 * @return The Tab at the given position.
	 */
	public Tab getItem(int position)
	{
		return mTabs.get(position);
	}


	/**
	 * Get one of the visible {@link Tab}s by its position.
	 * 
	 * @param position
	 *            The position among the visible items.
	 * @return The Tab at the given position.
	 */
	public Tab getVisibleItem(int position)
	{
		return mVisible.get(position);
	}


	/**
	 * Get the number of all {@link Tab}s.
	 * 
	 * @return The number of tabs.
	 */
	public int size()
	{
		return mTabs.size();
	}


	/**
	 * Get the number of visible {@link Tab}s.
	 * 
	 * @return The number of visible tabs.
	 */
	public int visibleSize()
	{
		return mVisible.size();
	}


	/**
	 * Update the internal list of visible tabs.
	 */
	private void updateVisible()
	{
		List<Tab> visible = mVisible;
		if (visible == null)
		{
			visible = mVisible = new ArrayList<Tab>(mTabs.size());
		}

		visible.clear();
		for (Tab tab : mTabs)
		{
			if (tab.visible)
			{
				visible.add(tab);
			}
		}
	}
}