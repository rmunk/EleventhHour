package hr.nas2skupa.eleventhhour.ui;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.GregorianCalendar;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Service;
import hr.nas2skupa.eleventhhour.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.ui.viewholders.ServiceViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_recycler_view)
public class ServicesFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    @FragmentArg
    String providerKey;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private String serviceKey;
    private Service selectedService;
    private GregorianCalendar pickedDateTime;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("services").child(providerKey).orderByChild("name");
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(
                Service.class,
                R.layout.item_service,
                ServiceViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ServiceViewHolder viewHolder, final Service model, int position) {
                final DatabaseReference categoryRef = getRef(position);
                serviceKey = categoryRef.getKey();
                selectedService = model;

                viewHolder.bindToService(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                ServicesFragment.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.setTitle(model.getName());
                        dpd.setMinDate(now);
                        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTransitions() {
        setAllowEnterTransitionOverlap(false);
        setAllowReturnTransitionOverlap(false);

        setEnterTransition(new Fade());
        setReenterTransition(new Fade());
        setExitTransition(new Slide(Gravity.TOP));
        setReturnTransition(new Slide(Gravity.TOP));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        pickedDateTime = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                ServicesFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);
        tpd.setTitle(selectedService.getName());
        tpd.setTimeInterval(1, 15);
        if (now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == monthOfYear && now.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
            tpd.setMinTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
        tpd.show(getActivity().getFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        pickedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        pickedDateTime.set(Calendar.MINUTE, minute);
        pickedDateTime.set(Calendar.SECOND, second);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment last = getFragmentManager().findFragmentByTag("dialog");
        if (last != null) transaction.remove(last);
        transaction.addToBackStack(null);

        BookingDialogFragment_.builder()
                .dateTime(pickedDateTime)
                .name(selectedService.getName())
                .price(selectedService.getPrice())
                .duration(selectedService.getDuration())
                .build()
                .show(getFragmentManager(), "dialog");
    }
}
