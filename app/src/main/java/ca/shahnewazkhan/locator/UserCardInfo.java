package ca.shahnewazkhan.locator;

import android.location.Location;

/**
 * Created by sparqy on 05/07/15.
 */
public class UserCardInfo implements Comparable<UserCardInfo>{

    protected String name;
    protected String distance;
    protected String fb_id;

    public int compareTo(UserCardInfo other) {

        return Double.compare(Double.parseDouble(distance), Double.parseDouble(other.distance));
    }
}
