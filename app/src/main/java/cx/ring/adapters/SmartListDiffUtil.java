package cx.ring.adapters;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import cx.ring.smartlist.SmartListViewModel;

public class SmartListDiffUtil extends DiffUtil.Callback {

    private List<SmartListViewModel> mOldList;
    private List<SmartListViewModel> mNewList;

    public SmartListDiffUtil(List<SmartListViewModel> oldList, List<SmartListViewModel> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mNewList.get(newItemPosition).getContact() == mOldList.get(oldItemPosition).getContact();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mNewList.get(newItemPosition).equals(mOldList.get(oldItemPosition));
    }
}
