package com.folkcat.demo.aidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.folkcat.demo.R;

import java.util.List;

/**
 * Created by Tamas on 2016/8/2.
 */
public class AIDLActivity extends Activity {
    private Button mBtnAddBook;

    private static final String TAG="AIDLActivity";

    //由AIDL文件生成的Java类
    private BookManager mBookManager = null;

    //标志当前与服务端连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound = false;

    //包含Book对象的list
    private List<Book> mBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl);

        mBtnAddBook=(Button)findViewById(R.id.btn_addbook);
        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook(mBtnAddBook);
            }
        });

    }

    /**
     * 按钮的点击事件，点击之后调用服务端的addBookIn方法
     *
     * @param view
     */
    public void addBook(View view) {
        //如果与服务端的连接处于未连接状态，则尝试连接
        if (!mBound) {
            attemptToBindService();
            Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBookManager == null) return;

        Book book = new Book();
        book.setName("APP研发录In");
        book.setPrice(30);
        try {
            mBookManager.addBook(book);
            Log.e(TAG, book.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 尝试与服务端建立连接
     */
    private void attemptToBindService() {
        Log.i(TAG,"attemptToBindService is called");
        /*
        Intent intent = new Intent();
        intent.setAction("com.folkcat.aidl");
        intent.setPackage("com.folkcat.demo");
        */
        Intent intent=new Intent(AIDLActivity.this,AIDLService.class);
        intent.setPackage("com.folkcat.demo");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "service connected");
            mBookManager = BookManager.Stub.asInterface(service);
            mBound = true;

            if (mBookManager != null) {
                try {
                    mBooks = mBookManager.getBooks();
                    Log.e(TAG, mBooks.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "service disconnected");
            mBound = false;
        }
    };
}