package com.example.myewaste.utils;

import com.example.myewaste.R;
import com.example.myewaste.model.utils.Task;

import java.util.ArrayList;

public class TaskData {

    private static final int[] taskTellerTitle = {
            R.string.report,
            R.string.report
    };

    private static final int[] taskTellerDesc = {
            R.string.customer_withdraw,
            R.string.customer_ballance
    };

    private static final int[] taskTellerIcon = {
            R.drawable.ic_transaction_saldo,
            R.drawable.ic_transaction_item
    };

    public static ArrayList<Task> getTaskTeller() {
        ArrayList<Task> list = new ArrayList<>();
        for (int i = 0; i < taskTellerTitle.length; i++) {
            Task task = new Task();
            task.setTitle(taskTellerTitle[i]);
            task.setDesc(taskTellerDesc[i]);
            task.setIcon(taskTellerIcon[i]);
            list.add(task);
        }
        return list;
    }


    private static final int[] taskAdminTitle = {
            R.string.master,
            R.string.master,
            R.string.master,
            R.string.report,
            R.string.report,
            R.string.data,
            R.string.data,
            R.string.data,
            R.string.data,
            R.string.data
    };


    private static final int[] taskAdminDesc = {
            R.string.item,
            R.string.item_type,
            R.string.unit,
            R.string.customer_ballance,
            R.string.customer_withdraw,
            R.string.nasabah,
            R.string.teller,
            R.string.admin,
            R.string.ballance,
            R.string.operational_cost
    };

    private static final int[] taskAdminIcon = {
            R.drawable.ic_item_master,
            R.drawable.ic_item_type,
            R.drawable.ic_item_unit,
            R.drawable.ic_transaction_item,
            R.drawable.ic_transaction_saldo,
            R.drawable.ic_data_nasabah,
            R.drawable.ic_data_teller,
            R.drawable.ic_data_admin,
            R.drawable.ic_data_saldo_nasabah,
            R.drawable.ic_cost_operational
    };

    public static ArrayList<Task> getTaskAdmin() {
        ArrayList<Task> list = new ArrayList<>();
        for (int i = 0; i < taskAdminTitle.length; i++) {
            Task task = new Task();
            task.setTitle(taskAdminTitle[i]);
            task.setDesc(taskAdminDesc[i]);
            task.setIcon(taskAdminIcon[i]);
            list.add(task);
        }
        return list;
    }


}
