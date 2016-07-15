package com.example.rafa.liquidgalaxypoiscontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rafa.liquidgalaxypoiscontroller.data.POIsContract;
import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by lgwork on 13/07/16.
 */
public class TreeItemHolder extends TreeNode.BaseNodeViewHolder<TreeItemHolder.IconTreeItem> {

    private TextView tvValue;
    private ImageView arrowView;
    private ImageView deleteButton;
    private ImageView addCategoryButton;
    private ImageView addPOIButton;
    private ImageView editButton;


    public TreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_profile_node, null, false);
        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);

        if (value.type == 1) {
            if (node.getId() % 2 == 0) {
                view.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }

        final ImageView iconView = (ImageView) view.findViewById(R.id.imageIcon);
        iconView.setImageDrawable(context.getResources().getDrawable(value.icon));


        arrowView = (ImageView) view.findViewById(R.id.arrow_icon);
        addCategoryButton = (ImageView) view.findViewById(R.id.btn_addCategory);
        addPOIButton = (ImageView) view.findViewById(R.id.btn_addPOI);
        editButton = (ImageView) view.findViewById(R.id.btn_edit);

        if (value.type == 1) {
            //It's a POI
            arrowView.setVisibility(View.GONE);
            addCategoryButton.setVisibility(View.GONE);
            addPOIButton.setVisibility(View.GONE);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent updateCategoryIntent = new Intent(context, UpdateItemActivity.class);
                    updateCategoryIntent.putExtra("UPDATE_TYPE", "POI");
                    updateCategoryIntent.putExtra("ITEM_ID", String.valueOf(value.id));
                    context.startActivity(updateCategoryIntent);
                }
            });

        } else {
            //It's a category
            addCategoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent createCategoryIntent = new Intent(context, CreateItemActivity.class);
                    createCategoryIntent.putExtra("CREATION_TYPE", "CATEGORY/HERENEW");
                    //FIXME: Review
                    createCategoryIntent.putExtra("CATEGORY_ID", String.valueOf(value.id));
                    context.startActivity(createCategoryIntent);
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent updateCategoryIntent = new Intent(context, UpdateItemActivity.class);
                    updateCategoryIntent.putExtra("UPDATE_TYPE", "CATEGORY");
                    updateCategoryIntent.putExtra("ITEM_ID", String.valueOf(value.id));
                    context.startActivity(updateCategoryIntent);
                }
            });

            if (node.getLevel() == 1) {
                addPOIButton.setVisibility(View.GONE);
            } else {
                addPOIButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent createPoiIntent = new Intent(context, CreateItemActivity.class);
                        createPoiIntent.putExtra("CREATION_TYPE", "POI/HERENEW");
                        //FIXME: Review
                        createPoiIntent.putExtra("CATEGORY_ID", String.valueOf(value.id));
                        context.startActivity(createPoiIntent);
                    }
                });
            }
        }

        deleteButton = (ImageView) view.findViewById(R.id.btn_delete);
        if (value.isDeletable) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(context.getResources().getString(R.string.are_you_sure));

                    alert.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (value.type == 1) {
                                //It's a POI
                                POIsContract.POIEntry.deletePOIById(context, String.valueOf(value.id));
                            } else {
                                //It's a Category
                                POIsContract.CategoryEntry.deleteCategoryById(context, String.valueOf(value.id));
                                //TODO: Delete category contents?
                            }
                            getTreeView().removeNode(node);
                        }
                    });

                    alert.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });
        } else {
            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrowView.setImageDrawable(context.getResources().getDrawable(active ? R.drawable.ic_keyboard_arrow_down_black_24dp : R.drawable.ic_keyboard_arrow_right_black_24dp));
    }

    public static class IconTreeItem {
        public String text;
        public int icon;
        public int type;
        public boolean isDeletable;
        public long id;

        public IconTreeItem(int icon, String text, long id, int type, boolean isDeletable) {
            this.icon = icon;
            this.text = text;
            this.type = type;
            this.isDeletable = isDeletable;
            this.id = id;
        }
    }

}
