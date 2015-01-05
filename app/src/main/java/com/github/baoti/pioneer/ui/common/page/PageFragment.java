package com.github.baoti.pioneer.ui.common.page;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.github.baoti.pioneer.R;
import com.github.baoti.pioneer.ui.common.FragmentView;
import com.github.baoti.pioneer.ui.common.Presenter;

import java.util.Collection;

import static butterknife.ButterKnife.findById;

/**
 * Created by liuyedong on 2015/1/2.
 */
public abstract class PageFragment<E> extends FragmentView<IPageView<E>, PagePresenter<E>> implements IPageView<E> {

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    protected LinearLayoutManager layoutManager;
    private PageAdapter<E> adapter;

    private boolean swipeRefreshEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        swipeRefreshLayout = findById(view, R.id.srl_swipe_refresh);
        recyclerView = findById(view, R.id.rv_recycler);
        layoutManager = createLinearLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int lastItem = layoutManager.getItemCount() - 1;
                if (lastVisibleItem >= lastItem && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    getPresenter().loadNextPage();
                }
                onRecyclerViewScrollStateChanged(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateSwipeRefreshLayoutEnabled();
                onRecyclerViewScrolled(dx, dy);
            }
        });
        swipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean prepared;

            @Override
            public void onGlobalLayout() {
                if (!prepared) {
                    prepared = true;
                    getPresenter().onSwipeRefreshPrepared();
                }
            }
        });

        setRetainInstance(true);
        super.onViewCreated(view, savedInstanceState);
    }

    protected int getLayoutRes() {
        return R.layout.swipe_recycler_view;
    }

    protected LinearLayoutManager createLinearLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    protected void onRecyclerViewScrollStateChanged(int newState) {

    }

    protected void onRecyclerViewScrolled(int dx, int dy) {

    }

    private void updateSwipeRefreshLayoutEnabled() {
        boolean enableRefresh = layoutManager.findFirstCompletelyVisibleItemPosition() == 0;
        swipeRefreshLayout.setEnabled(swipeRefreshEnabled && enableRefresh);
    }

    @Override
    public void onPresenterTaken(Presenter presenter) {
        swipeRefreshLayout.setOnRefreshListener(getPresenter());
        adapter = createPageAdapter(LayoutInflater.from(getActivity()), getPresenter());
        recyclerView.setAdapter(adapter);
    }

    protected abstract PageAdapter<E> createPageAdapter(LayoutInflater layoutInflater, PagePresenter<E> presenter);

    @Override
    public void showResources(Collection<E> resources, int start, int before, int count) {
        adapter.changeItems(resources, start, before, count);
    }

    @Override
    public void showRefreshing() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void updateLoadingMore() {
        if (layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
            ((PageAdapter) recyclerView.getAdapter()).notifyLoadingChanged();
        }
    }

    @Override
    public void enableSwipeRefreshing() {
        swipeRefreshEnabled = true;
        updateSwipeRefreshLayoutEnabled();
    }

    @Override
    public void disableSwipeRefreshing() {
        swipeRefreshEnabled = false;
        updateSwipeRefreshLayoutEnabled();
    }
}
