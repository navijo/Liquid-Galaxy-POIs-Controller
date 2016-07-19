package com.example.rafa.liquidgalaxypoiscontroller;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rafa.liquidgalaxypoiscontroller.beans.Category;
import com.example.rafa.liquidgalaxypoiscontroller.beans.POI;
import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.example.rafa.liquidgalaxypoiscontroller.utils.CustomAndroidTreeView;
import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by lgwork on 13/07/16.
 */
public class NewPOISList extends Fragment {

    private CustomAndroidTreeView tView;

    public static NewPOISList newInstance() {
        Bundle args = new Bundle();

        NewPOISList fragment = new NewPOISList();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_fragment_pois, null, false);
        ViewGroup containerView = (ViewGroup) rootView.findViewById(R.id.container);

        TreeNode root = TreeNode.root();

        //getRootCategories => those with father id = 0
        //foreach rootCategory => get child categories recursively

        TreeNode categoriesRoot = new TreeNode(new TreeItemHolder.IconTreeItem(R.drawable.ic_home_black_24dp, getResources().getString(R.string.categoriesRoot), 0, 0, false));

        try (Cursor rootCategories = POIsContract.CategoryEntry.getRootCategories(getActivity())) {
            while (rootCategories.moveToNext()) {
                final Category rootCategory = getCategoryData(rootCategories);
                TreeItemHolder.IconTreeItem parentNode;

                switch (rootCategory.getName()) {
                    case "earth":
                    case "EARTH":
                        parentNode = new TreeItemHolder.IconTreeItem(R.drawable.earth, rootCategory.getName(), rootCategory.getId(), 0, false);
                        break;
                    case "moon":
                    case "MOON":
                        parentNode = new TreeItemHolder.IconTreeItem(R.drawable.moon, rootCategory.getName(), rootCategory.getId(), 0, false);
                        break;
                    case "mars":
                    case "MARS":
                        parentNode = new TreeItemHolder.IconTreeItem(R.drawable.mars, rootCategory.getName(), rootCategory.getId(), 0, false);
                        break;
                    default:
                        parentNode = new TreeItemHolder.IconTreeItem(R.drawable.ic_home_black_24dp, rootCategory.getName(), rootCategory.getId(), 0, false);
                        break;
                }

                final TreeNode parent = new TreeNode(parentNode).setViewHolder(new TreeItemHolder(getActivity()));

//                new Thread(new Runnable() {
//                    public void run() {
//                        Looper.prepare();
//                        try {
                            getChildCategories(rootCategory, parent);
                            getPois(rootCategory, parent);
//                        } catch (Exception e) {
//                            Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }).start();

                categoriesRoot.addChild(parent);
            }
            rootCategories.close();
        }

        root.addChildren(categoriesRoot);

        tView = new CustomAndroidTreeView(getActivity(), root);
        tView.setDefaultAnimation(false);
//        tView.setUseAutoToggle(false);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(TreeItemHolder.class);

        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }
        return rootView;
    }

    private void getPois(Category category, TreeNode parent) {

        try (Cursor poisInCategory = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(category.getId()))) {
            while (poisInCategory.moveToNext()) {
                POI poi = getPoiData(poisInCategory);
                TreeNode poiNode = new TreeNode(new TreeItemHolder.IconTreeItem(R.drawable.ic_place_black_24dp, poi.getName(), poi.getId(), 1, true));
                parent.addChild(poiNode);
            }
            poisInCategory.close();
        }
    }


    private void getChildCategories(Category parentCategory, TreeNode parent) {
        try (Cursor childCategories = POIsContract.CategoryEntry.getCategoriesByFatherID(getActivity(), String.valueOf(parentCategory.getId()))) {
            while (childCategories.moveToNext()) {
                final Category childCategory = getCategoryData(childCategories);
                int count = POIsContract.POIEntry.countPOIsByCategory(getActivity(), String.valueOf(childCategory.getId()));
                final TreeNode childCategoryNode = new TreeNode(new TreeItemHolder.IconTreeItem(R.drawable.ic_folder_black_24dp, childCategory.getName() + " (" + count + ") ", childCategory.getId(), 0, true));
//                new Thread(new Runnable() {
//                    public void run() {
//                        try {
                            getChildCategories(childCategory, childCategoryNode);
                            getPois(childCategory, childCategoryNode);
//                        } catch (Exception e) {
//                            Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }).start();
                parent.addChild(childCategoryNode);
            }
            childCategories.close();
        }
    }

    private POI getPoiData(Cursor poisInCategory) {
        POI poi = new POI();
        poi.setName(poisInCategory.getString(poisInCategory.getColumnIndex(POIsContract.POIEntry.COLUMN_COMPLETE_NAME)));
        poi.setId(poisInCategory.getInt(poisInCategory.getColumnIndex(POIsContract.POIEntry.COLUMN_ID)));
        return poi;
    }

    private Category getCategoryData(Cursor rootCategories) {
        Category category = new Category();
        category.setName(rootCategories.getString(rootCategories.getColumnIndex(POIsContract.CategoryEntry.COLUMN_NAME)));
        category.setId(rootCategories.getInt(rootCategories.getColumnIndex(POIsContract.CategoryEntry.COLUMN_ID)));
        return category;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

}
