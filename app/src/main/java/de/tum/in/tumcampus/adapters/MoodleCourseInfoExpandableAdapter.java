package de.tum.in.tumcampus.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleCourseContent;
import de.tum.in.tumcampus.models.MoodleCourseModule;
import de.tum.in.tumcampus.models.MoodleCourseSection;
import de.tum.in.tumcampus.models.MoodleObject;

/**
 * Created by a2k on 6/11/2015.
 * the data provided by moodle is a list of jsons for each there is a section,
 * each section has everal modules, and each module can have several contents, which
 * can be file, link or ...
 * in this adapter section and modules are considered as group items, and contents as childs
 * Therefore, sections has no child, but each module can have several childs
 */
public class MoodleCourseInfoExpandableAdapter extends BaseExpandableListAdapter{

    private List<MoodleCourseSection> sections;

   // a mapping from keys=("sections","modules") to positions in group indexes
    private Map<String, List<Integer>> indexMapping;

    // all group positions for existing sections
    private Map<Integer, MoodleCourseSection> exisiting_sections;

    // all group positions for existing modules
    private Map<Integer, MoodleCourseModule> exisiting_modules;

    private final Activity activity;
    private LayoutInflater inflater = null;

    // types for group object
    final int SECTION_HEADER = 1000;
    final int MODULE_HEADER = 500;
    final int NOT_KNOWN = 0;

    // types for drawable indicator icon
    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // index 0
            GROUP_EXPANDED_STATE_SET // index 1
    };

    public static class ViewHolder {
        public ImageView image;
        public TextView title;
    }

    public MoodleCourseInfoExpandableAdapter(List<MoodleCourseSection> sections, Activity activity) {

        if (sections == null) {
            Utils.log("The given List of values for MoodleCourseInfoAdapter is null");
            MoodleCourseSection temp = new MoodleCourseSection(null);
            this.sections = new ArrayList<>();
            this.sections.add(temp);
        } else
            this.sections = sections;

        make_indexMapping();
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getGroupCount() {
        return exisiting_sections.size() + exisiting_modules.size();
    }

    /**
     * return 0 for Sections of the course and also for Modules which
     have no content
     * @param groupPosition
     * @return the children count
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            if (getItemViewType(groupPosition) == MODULE_HEADER) {
                List<MoodleCourseContent> contents = getModule(groupPosition).getContents();
                if (contents != null)
                    return contents.size();
                else
                    return 0;
            } else
                return 0;
        }catch(Exception e){
            Utils.log(e);
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {

        if (getItemViewType(groupPosition) == SECTION_HEADER)
            return getSection(groupPosition);
        else if (getItemViewType(groupPosition) == MODULE_HEADER)
            return getModule(groupPosition);
        else
            Utils.log(String.format("No Group Object Found for position %s", groupPosition));
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            if (getItemViewType(groupPosition) == MODULE_HEADER) {
                List<MoodleCourseContent> contents = (List<MoodleCourseContent>) getModule(groupPosition).getContents();
                if (contents != null)
                    return contents.get(childPosition);
                else
                    return null;
            }
            return null;
        }catch (Exception e){
            Utils.log(e);
            return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int type = getItemViewType(groupPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.moodle_course_headers, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.header);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.indicator_icon);
            convertView.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder)convertView.getTag();

        try {
            switch (type) {

                // setting the necessary style based on the type of the group item
                case SECTION_HEADER:
                    MoodleCourseSection section_item = (MoodleCourseSection) getGroup(groupPosition);
                    changeStyle(viewHolder, section_item, groupPosition, isExpanded);
                    break;
                case MODULE_HEADER:
                    MoodleCourseModule module_item = (MoodleCourseModule) getGroup(groupPosition);
                    changeStyle(viewHolder, module_item, groupPosition, isExpanded);
                    break;
                case NOT_KNOWN:
                    Utils.log(String.format("Error: Item %s type not found", groupPosition));
                    break;
            }
        } catch (NullPointerException e) {
            Utils.log(e);
            Utils.log(String.format("Error: Item %s is null", groupPosition));
            return convertView;
        }

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        int type = getItemViewType(groupPosition);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.moodle_course_content, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.courseChildHeader);
            convertView.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder)convertView.getTag();

        try {
            switch (type) {
                case MODULE_HEADER:
                    MoodleCourseContent content = (MoodleCourseContent) getChild(groupPosition, childPosition);
                    if (content != null) {
                        String name = content.getFilename();
                        viewHolder.title.setText(name);
                    }
            }
        }catch (NullPointerException e) {
            Utils.log(e);
            Utils.log(String.format("Error: Item %s is null", groupPosition));
            return convertView;
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * gets back the type of the item in group positions of the list
     * @param position the position of the grouo item
     * @return (int) type of this group: module or section
     */
    public int getItemViewType(int position) {
        if (sections == null)
            return NOT_KNOWN;
        else {
            if (indexMapping.get("sections").contains(Integer.valueOf(position)))
                return SECTION_HEADER;
            else if (indexMapping.get("modules").contains(Integer.valueOf(position)))
                return MODULE_HEADER;
            else
                return NOT_KNOWN;
        }
    }

    /**
     * This method make a mapping for indexes in the list of items shown
     * in the activity and their type (section?module?content based on the position in th layout)
     */
    private void make_indexMapping() {
        indexMapping = new HashMap<>();
        List<Integer> sections_indexes = new ArrayList<>();
        List<Integer> modules_indexes = new ArrayList<>();
        if (sections != null) {
            exisiting_sections = new HashMap<>();
            exisiting_modules = new HashMap<>();

            int index = -1;
            for (MoodleCourseSection currentSection : sections) {
                index++;
                exisiting_sections.put(index, currentSection);
                sections_indexes.add(index);

                if (currentSection.getModules() != null) {

                    for (MoodleCourseModule currentModule : currentSection.getModules()) {
                        index++;
                        exisiting_modules.put(index, currentModule);
                        modules_indexes.add(index);
                    }
                }
            }
            indexMapping.put("sections", sections_indexes);
            indexMapping.put("modules", modules_indexes);
            Utils.log("Current mapping found for this course");
            Utils.log("sections: " + indexMapping.get("sections").toString());
            Utils.log("modules: " + indexMapping.get("modules").toString());

        } else
            Utils.log("cannot make index_mapping! Sections is null");
    }


    public MoodleCourseSection getSection(int position) {

        return exisiting_sections.get(position);
    }

    public MoodleCourseModule getModule(int position) {
        return exisiting_modules.get(position);
    }

    /**
     * gets the drawable for related module in moodle
     * because of efficiency used this method, retrieving resources by string name
     * is not efficient.
     * @param module of type MoodleCourseModule
     * @return Drawable, the icon related to this module
     */
    public Drawable getImageResource(MoodleCourseModule module){
        String modName = module.getModname();
        switch(modName){
            case "assign":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_assignment);
            case "assignment":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_assignment);
            case "book":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_book);
            case "chat":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_chat);
            case "choice":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_choice);
            case "data":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_data);
            case "feedback":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_feedback);
            case "folder":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_folder);
            case "forum":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_forum);
            case "glossary":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_glossary);
            case "imscp":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_imscp);
            case "label":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_label);
            case "lesson":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_lesson);
            case "lti":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_lti);
            case "page":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_page);
            case "quiz":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_quiz);
            case "resource":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_resource);
            case "scorm":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_scorm);
            case "survey":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_survey);
            case "url":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_url);
            case "wiki":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_wiki);
            case "workshop":
                return this.activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_workshop);
            default:
                return null;
        }
    }

    /**
     * calculates the image height for the imageView of an item
     * in the list. Based on the item type. Group, Group with children, child
     * @param itemType
     * @return height in pixels based on the display density of the device
     */
    public int getImageHeight(int itemType){
        DisplayMetrics metrics = this.activity.getApplicationContext().getResources().getDisplayMetrics();
        float dp, fpixels;
        switch(itemType){
            case SECTION_HEADER:
                dp = 30f;
                fpixels = metrics.density * dp;
                return (int) (fpixels + 0.5f);
            case MODULE_HEADER:
                dp = 80f;
                fpixels = metrics.density * dp;
                return (int) (fpixels + 0.5f);
            default:
                return 0;
        }
    }

    private void changeStyle(ViewHolder holder, MoodleObject object, int groupPosition, boolean isExpanded){

        if (object instanceof MoodleCourseSection){
            MoodleCourseSection section = (MoodleCourseSection) object;
            String mod_name = section.getName();
            holder.image.setVisibility(View.GONE);
            holder.title.setHeight(getImageHeight(SECTION_HEADER));
            holder.title.setText(mod_name);
            holder.title.setBackgroundColor(Color.argb(246,244,120,3));
        }else{

            // the object is a course Module
            MoodleCourseModule module = (MoodleCourseModule) object;
            String mod_name = module.getName();
            holder.title.setHeight(getImageHeight(MODULE_HEADER));
            holder.title.setText(mod_name);
            holder.title.setBackgroundColor(Color.WHITE);

            /* if this course module has no child set the icon
            from getImageResource, otherwise set the group indicator icon
            */
            if( getChildrenCount( groupPosition ) == 0 ) {
                Drawable drawable = getImageResource(module);
                if (drawable == null)
                    holder.image.setVisibility(View.GONE);
                else {
                    holder.image.setImageDrawable(drawable);
                    holder.image.setVisibility( View.VISIBLE );
                }
            }else {
                holder.image.setVisibility( View.VISIBLE );
                Drawable drawable = activity.getBaseContext().getResources().getDrawable(R.drawable.moodle_expander_group);
                holder.image.setImageDrawable(drawable);

                // setting the state of the group indicator
                int stateSetIndex = ( isExpanded ? 1 : 0) ;
                drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
            }
        }
    }
}