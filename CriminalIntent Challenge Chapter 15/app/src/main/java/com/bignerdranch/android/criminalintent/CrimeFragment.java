package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

/**
 * Created by julianlo on 11/30/15.
 */
public class CrimeFragment extends Fragment {

    // Not the best encapsulation. Now CrimeListFragment needs to know that CrimeActivity uses CrimeFragment.
    // But at least this keeps CrimeFragment ignorant of its hosting activity.
    public static final String EXTRA_CRIME_ID_MODIFIED = "com.bignerdranch.android.criminalintent.crime_id_modified";

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCrime = CrimeLab.get(getActivity()).getCrime((UUID)getArguments().getSerializable(ARG_CRIME_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No op
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(getFragmentManager(), DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button)v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel: " + mCrime.getPhone()));
                startActivity(intent);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        String suspect = mCrime.getSuspect();
        if (suspect != null) {
            mSuspectButton.setText(suspect);
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        String phone = mCrime.getPhone();
        if (phone != null) {
            mReportButton.setText(phone);
        } else {
            mReportButton.setEnabled(false);
        }

        //returnResult();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();

            // Specify which fields you want your query to return values for.
            String[] queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };

            // Perform your query - the contactUri is like a "where" clause here
            Cursor contactCursor = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            try {
                // Double-check that you actually got results
                if (contactCursor.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data - that is your suspect's name.
                contactCursor.moveToFirst();
                String suspect = contactCursor.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                // Pull out the second column - that is the suspect's contact ID.
                String contactId = contactCursor.getString(1);
                String[] phoneQueryFields = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                Cursor phoneCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneQueryFields,
                        selection,
                        new String[]{contactId},
                        null);

                try {
                    if (phoneCursor.getCount() == 0) {
                        return;
                    }

                    phoneCursor.moveToFirst();
                    String phone = phoneCursor.getString(0);
                    mCrime.setPhone(phone);
                    mReportButton.setText(phone);
                    mReportButton.setEnabled(true);
                } finally {
                    phoneCursor.close();
                }

            } finally {
                contactCursor.close();
            }

            String phone;


        }
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getLongDateFormat(getActivity()).format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = getString(mCrime.isSolved() ? R.string.crime_report_solved : R.string.crime_report_unsolved);

        String dateFormat = "EEE, MM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = (mCrime.getSuspect() == null) ?
                getString(R.string.crime_report_no_suspect) :
                getString(R.string.crime_report_suspect, mCrime.getSuspect());

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    /*
    private void returnResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_CRIME_ID_MODIFIED, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
    }
    */
}
