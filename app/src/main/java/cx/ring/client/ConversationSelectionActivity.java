package cx.ring.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;
import javax.inject.Singleton;

import cx.ring.R;
import cx.ring.adapters.SmartListAdapter;
import cx.ring.application.JamiApplication;
import cx.ring.facades.ConversationFacade;
import cx.ring.model.Account;
import cx.ring.services.CallService;
import cx.ring.smartlist.SmartListViewModel;
import cx.ring.utils.ConversationPath;
import cx.ring.viewholders.SmartListViewHolder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class ConversationSelectionActivity extends AppCompatActivity {
    private final static String TAG = ConversationSelectionActivity.class.getSimpleName();

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Inject
    @Singleton
    ConversationFacade mConversationFacade;

    @Inject
    @Singleton
    CallService mCallService;

    private SmartListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        setContentView(R.layout.frag_selectconv);
        RecyclerView list = findViewById(R.id.conversationList);

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);*/

        adapter = new SmartListAdapter(null, new SmartListViewHolder.SmartListListeners() {
            @Override
            public void onItemClick(SmartListViewModel smartListViewModel) {
                Intent intent = new Intent();
                intent.setData(ConversationPath.toUri(smartListViewModel.getAccountId(), smartListViewModel.getContact().getPrimaryNumber()));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onItemLongClick(SmartListViewModel smartListViewModel) {
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mDisposable.add(mConversationFacade
                .getCurrentAccountSubject()
                .switchMap(Account::getConversationsViewModels)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (adapter != null)
                        adapter.update(list);
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        mDisposable.clear();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter = null;
    }
}
