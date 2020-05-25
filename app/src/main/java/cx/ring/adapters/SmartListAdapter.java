package cx.ring.adapters;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cx.ring.databinding.ItemSmartlistBinding;
import cx.ring.smartlist.SmartListViewModel;
import cx.ring.viewholders.SmartListViewHolder;

public class SmartListAdapter extends RecyclerView.Adapter<SmartListViewHolder> {

    private List<SmartListViewModel> mSmartListViewModels = new ArrayList<>();
    private SmartListViewHolder.SmartListListeners listener;
    private RecyclerView recyclerView;

    public SmartListAdapter(List<SmartListViewModel> smartListViewModels, SmartListViewHolder.SmartListListeners listener) {
        this.listener = listener;
        if (smartListViewModels != null)
            mSmartListViewModels.addAll(smartListViewModels);
    }

    @NonNull
    @Override
    public SmartListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemSmartlistBinding itemBinding = ItemSmartlistBinding.inflate(layoutInflater, parent, false);
        return new SmartListViewHolder(itemBinding);
    }

    @Override
    public void onViewRecycled(@NonNull SmartListViewHolder holder) {
        super.onViewRecycled(holder);
        holder.unbind();
    }

    @Override
    public void onBindViewHolder(@NonNull SmartListViewHolder holder, int position) {
        final SmartListViewModel smartListViewModel = mSmartListViewModels.get(position);
        holder.bind(listener, smartListViewModel);
    }

    @Override
    public int getItemCount() {
        return mSmartListViewModels.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void update(List<SmartListViewModel> viewModels) {
        final List<SmartListViewModel> old = mSmartListViewModels;
        mSmartListViewModels = viewModels == null ? new ArrayList<>() : viewModels;
        if (old != null && viewModels != null) {
            Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            DiffUtil.calculateDiff(new SmartListDiffUtil(old, viewModels))
                    .dispatchUpdatesTo(this);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        } else {
            notifyDataSetChanged();
        }
    }

    public void update(SmartListViewModel smartListViewModel) {
        for (int i = 0; i < mSmartListViewModels.size(); i++) {
            SmartListViewModel old = mSmartListViewModels.get(i);
            if (old.getContact() == smartListViewModel.getContact()) {
                mSmartListViewModels.set(i, smartListViewModel);
                notifyItemChanged(i);
            }
        }
    }
}