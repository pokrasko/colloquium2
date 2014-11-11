package ru.ifmo.md.colloquium2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by pokrasko on 11.11.14.
 */
public class VoteAdapter extends BaseAdapter {
    private PersonList list;

    public VoteAdapter(PersonList list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.person_item, viewGroup, false);
        }
        final Person feed = list.get(i);

        TextView name = (TextView) view.findViewById(R.id.feedTitle);
        TextView description = (TextView) view.findViewById(R.id.feedDescription);
        title.setText(feed.getTitle());
        description.setText(feed.getDescription());

        return view;
    }

    public void add(Person person) {
        list.add(person);
        notifyDataSetChanged();
    }

    public void rename(int i, String name) {
        list.rename(i, name);
        notifyDataSetChanged();
    }

    public void vote(int i) {
        list.vote(i);
        notifyDataSetChanged();
    }

    public void remove(int i) {
        list.remove(i);
        notifyDataSetChanged();
    }
}
