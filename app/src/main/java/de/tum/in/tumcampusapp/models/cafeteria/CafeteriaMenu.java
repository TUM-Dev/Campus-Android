package de.tum.in.tumcampusapp.models.cafeteria;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * CafeteriaMenu object
 */
public class CafeteriaMenu {

    /**
     * Cafeteria ID
     */
    public final int cafeteriaId;

    /**
     * Menu date
     */
    public final Date date;

    /**
     * CafeteriaMenu Id (empty for addendum)
     */
    public final int id;

    /**
     * Menu name
     */
    public final String name;

    /**
     * Long type, e.g. Tagesgericht 1
     */
    public final String typeLong;

    /**
     * Type ID
     */
    public final int typeNr;

    /**
     * Short type, e.g. tg
     */
    public final String typeShort;

    // public String prize;

    /**
     * New CafeteriaMenu
     *
     * @param id          CafeteriaMenu Id (empty for addendum)
     * @param cafeteriaId Cafeteria ID
     * @param date        Menu date
     * @param typeShort   Short type, e.g. tg
     * @param typeLong    Long type, e.g. Tagesgericht 1
     * @param typeNr      Type ID
     * @param name        Menu name
     */
    public CafeteriaMenu(int id, int cafeteriaId, Date date, String typeShort,
                         String typeLong, int typeNr, String name) {

        this.id = id;
        this.cafeteriaId = cafeteriaId;
        this.date = date;
        this.typeShort = typeShort;
        this.typeLong = typeLong;
        this.typeNr = typeNr;
        this.name = name;
    }

    @Override
    public String toString() {
        return "id=" + this.id + " cafeteriaId=" + this.cafeteriaId + " date="
               + Utils.getDateString(this.date) + " typeShort="
               + this.typeShort + " typeLong=" + this.typeLong + " typeNr="
               + this.typeNr + " name=" + this.name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CafeteriaMenu that = (CafeteriaMenu) o;

        if (cafeteriaId != that.cafeteriaId) {
            return false;
        }
        if (id != that.id) {
            return false;
        }
        if (typeNr != that.typeNr) {
            return false;
        }
        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (typeLong != null ? !typeLong.equals(that.typeLong) : that.typeLong != null) {
            return false;
        }
        return typeShort != null ? typeShort.equals(that.typeShort) : that.typeShort == null;

    }

    @Override
    public int hashCode() {
        int result = cafeteriaId;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (typeLong != null ? typeLong.hashCode() : 0);
        result = 31 * result + typeNr;
        result = 31 * result + (typeShort != null ? typeShort.hashCode() : 0);
        return result;
    }
}