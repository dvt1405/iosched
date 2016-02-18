/*
 * Copyright 2016 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.myschedule;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import com.google.samples.apps.iosched.Config;
import com.google.samples.apps.iosched.util.TimeUtils;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;
import static com.google.samples.apps.iosched.util.LogUtils.makeLogTag;

/**
 * The {@link MyScheduleActivity} uses a {@link android.support.v4.view.ViewPager} in narrow mode,
 * where each page shows the schedule for the day, using a {@link MyScheduleSingleDayFragment}.
 */
public class MyScheduleDayViewPagerAdapter extends FragmentPagerAdapter {

    private final static String TAG = makeLogTag(MyScheduleDayViewPagerAdapter.class);

    private Context mContext;

    private boolean mShowPreConferenceDay;

    private MyScheduleSingleDayFragment[] mFragments;

    public MyScheduleDayViewPagerAdapter(Context context, FragmentManager fm,
            boolean showPreConferenceDay) {
        super(fm);
        mShowPreConferenceDay = showPreConferenceDay;
        mContext = context;
    }

    @Override
    public MyScheduleSingleDayFragment getItem(int position) {
        LOGD(TAG, "Creating fragment #" + position);

        // Reuse cached fragment if present
        if (mFragments != null && mFragments.length > position && mFragments[position] != null) {
            return mFragments[position];
        }

        MyScheduleSingleDayFragment frag = new MyScheduleSingleDayFragment();
        Bundle args = new Bundle();

        // 1 for the first day of the conference, 2 for the second etc and 0 for the pre conference
        // day if any
        args.putInt(MyScheduleActivity.ARG_CONFERENCE_DAY_INDEX,
                mShowPreConferenceDay ? position : position + 1);

        frag.setArguments(args);
        if (mFragments == null) {
            mFragments = new MyScheduleSingleDayFragment[getCount()];
        }
        mFragments[position] = frag;
        return frag;
    }

    @Override
    public int getCount() {
        return Config.CONFERENCE_DAYS.length + (mShowPreConferenceDay ? 1 : 0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TimeUtils.getDayName(mContext, position - (mShowPreConferenceDay ? 1 : 0));
    }
}

