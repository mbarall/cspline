package scratch.iges;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.Writer;
import java.io.Reader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import scratch.util.MarshalWriter;
import scratch.util.MarshalReader;
import scratch.util.MarshalImpJsonWriter;
import scratch.util.MarshalImpJsonReader;
import scratch.util.MarshalException;


/**
 * Class for constructing and writing an IGES file containing B-spline surfaces.
 * Author: Michael Barall 11/18/2018.
 *
 * See documentation:
 * https://filemonger.com/specs/igs/devdept.com/version6.pdf
 * https://web.archive.org/web/20120821190122/http://www.uspro.org/documents/IGES5-3_forDownload.pdf
 * https://wiki.eclipse.org/IGES_file_Specification
 * https://en.wikipedia.org/wiki/IGES
 *
 */
public class IgesFile {


	//----- Global definitions -----


	// The line terminator.

	public static final String LINE_TERM = "\n";

	// The parameter separator.

	public static final String PARAM_SEP = ",";

	// The record terminator.

	public static final String REC_TERM = ";";

	// System identifier.

	public static final String SYSTEM_ID = "cspline";

	// Section letter codes.

	public static final String LC_START = "S";
	public static final String LC_GLOBAL = "G";
	public static final String LC_DIR_ENTRY = "D";
	public static final String LC_PARAM_DATA = "P";
	public static final String LC_TERMINATE = "T";

	// Content width for free-form data.

	public static final int CW_START = 72;
	public static final int CW_GLOBAL = 72;
	public static final int CW_PARAM_DATA = 64;

	// Numeric ranges.

	public static final int G_INT_BITS = 32;
	public static final int G_INT_MIN = -2147483647;
	public static final int G_INT_MAX = 2147483647;

	public static final int    G_FLOAT_EXP = 38;
	public static final int    G_FLOAT_DIGITS = 6;
	public static final double G_FLOAT_MIN = 1.0e-38;
	public static final double G_FLOAT_MAX = 1.0e38;

	public static final int    G_DOUBLE_EXP = 38;
	public static final int    G_DOUBLE_DIGITS = 15;
	public static final double G_DOUBLE_MIN = 1.0e-38;
	public static final double G_DOUBLE_MAX = 1.0e38;

	public static final int G_POINTER_MIN = -9999999;
	public static final int G_POINTER_MAX = 9999999;

	// Unit flags.

	public static final int UFLAG_INCH			= 1;
	public static final int UFLAG_MILLIMETER	= 2;
	public static final int UFLAG_UNSPECIFIED	= 3;
	public static final int UFLAG_FOOT			= 4;
	public static final int UFLAG_MILE			= 5;
	public static final int UFLAG_METER			= 6;
	public static final int UFLAG_KILOMETER		= 7;
	public static final int UFLAG_MIL			= 8;
	public static final int UFLAG_MICRON		= 9;
	public static final int UFLAG_CENTIMETER	= 10;
	public static final int UFLAG_MICROINCH		= 11;

	// Unit names.

	public static final String UNAME_INCH		= "IN";
	public static final String UNAME_MILLIMETER	= "MM";
	public static final String UNAME_FOOT		= "FT";
	public static final String UNAME_MILE		= "MI";
	public static final String UNAME_METER		= "M";
	public static final String UNAME_KILOMETER	= "KM";
	public static final String UNAME_MIL		= "MIL";
	public static final String UNAME_MICRON		= "UM";
	public static final String UNAME_CENTIMETER	= "CM";
	public static final String UNAME_MICROINCH	= "UIN";

	// Version flag.

	public static final int VERSION_FLAG		= 11;

	// Line font pattern numbers

	public static final int LINE_PATTERN_UNSPECIFIED	= 0;
	public static final int LINE_PATTERN_SOLID			= 1;
	public static final int LINE_PATTERN_DASHED			= 2;
	public static final int LINE_PATTERN_PHANTOM		= 3;
	public static final int LINE_PATTERN_CENTERLINE		= 4;
	public static final int LINE_PATTERN_DOTTED			= 5;

	// Entity status values (as strings), concatenate one in each category to get 8-char string.

	public static final int ESTAT_BLANK_VISIBLE			= 0;
	public static final int ESTAT_BLANK_BLANKED			= 1;

	public static final int ESTAT_SUBORD_INDEPENDENT	= 0;
	public static final int ESTAT_SUBORD_PHYSICAL		= 1;
	public static final int ESTAT_SUBORD_LOGICAL		= 2;
	public static final int ESTAT_SUBORD_BOTH			= 3;

	public static final int ESTAT_USE_GEOMETRY			= 0;
	public static final int ESTAT_USE_ANNOTATION		= 1;
	public static final int ESTAT_USE_DEFINITION		= 2;
	public static final int ESTAT_USE_OTHER				= 3;
	public static final int ESTAT_USE_POSITIONAL		= 4;
	public static final int ESTAT_USE_PARAMETRIC		= 5;
	public static final int ESTAT_USE_CONSTRUCTION		= 6;

	public static final int ESTAT_HIER_TOPDOWN			= 0;
	public static final int ESTAT_HIER_DEFER			= 1;
	public static final int ESTAT_HIER_HIERARCHY		= 2;

	// Color numbers.

	public static final int COLOR_NONE		= 0;
	public static final int COLOR_BLACK		= 1;
	public static final int COLOR_RED		= 2;
	public static final int COLOR_GREEN		= 3;
	public static final int COLOR_BLUE		= 4;
	public static final int COLOR_YELLOW	= 5;
	public static final int COLOR_MAGENTA	= 6;
	public static final int COLOR_CYAN		= 7;
	public static final int COLOR_WHITE		= 8;

	// Entity type code and forms: B-Spline surface.

	public static int ETYPE_B_SPLINE_SURFACE = 128;

	public static final int EFORM_BSS_DATA			= 0;
	public static final int EFORM_BSS_PLANE			= 1;
	public static final int EFORM_BSS_RT_CIRC_CYL	= 2;
	public static final int EFORM_BSS_CONE			= 3;
	public static final int EFORM_BSS_SPHERE		= 4;
	public static final int EFORM_BSS_TORUS			= 5;
	public static final int EFORM_BSS_SURF_OF_REV	= 6;
	public static final int EFORM_BSS_TAB_CYL		= 7;
	public static final int EFORM_BSS_RULED_SURF	= 8;
	public static final int EFORM_BSS_GEN_QUAD_SURF	= 9;




	//----- IGES data types -----




	// Base class for all IGES data types.
	// All IGES data type classes are immutable.

	public static abstract class IgesData {

		// Flag indicates if this value is defaulted.

		protected boolean f_defaulted;

		public boolean is_defaulted () {
			return f_defaulted;
		}

		// Hook method used by subclasses to return the string representation when not defaulted.

		protected abstract String iges_string_rep ();

		// Our string representation is an empty string if defaulted, otherwise the subclass representation.

		private String cached_string_rep = null;

		@Override
		public String toString () {
			if (f_defaulted) {
				return "";
			}
			if (cached_string_rep == null) {
				cached_string_rep = iges_string_rep();
			}
			return cached_string_rep;
		}

		// Return the representation right-justified in a field of 8 characters.

		public String fixed_rep () {
			String result = String.format ("%8s", toString());
			if (result.length() != 8) {
				throw new RuntimeException ("IgesData: Fixed representation conversion length error: [" + result + "]");
			}
			return result;
		}

		// Return the representation right-justified in a field of 7 characters.

		public String fixed_ptr_rep () {
			String result = String.format ("%7s", toString());
			if (result.length() != 7) {
				throw new RuntimeException ("IgesData: Fixed representation conversion length error: [" + result + "]");
			}
			return result;
		}

		// Conversion functions.

		public abstract String type_name ();

		public IgesInteger as_integer () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesInteger: [" + toString() + "]");
		}

		public IgesReal as_real () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesReal [" + toString() + "]");
		}

		public IgesFloat as_float () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesFloat [" + toString() + "]");
		}

		public IgesDouble as_double () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesDouble [" + toString() + "]");
		}

		public IgesStringBase as_string_base () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesStringBase [" + toString() + "]");
		}

		public IgesString as_string () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesString [" + toString() + "]");
		}

		public IgesPointer as_pointer () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesPointer [" + toString() + "]");
		}

		public IgesLiteralString as_literal_string () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesLiteralString [" + toString() + "]");
		}

		public IgesBoolean as_boolean () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesBoolean [" + toString() + "]");
		}

		public IgesIntegerOrPointer as_integer_or_pointer () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesIntegerOrPointer [" + toString() + "]");
		}

		public IgesDate as_date () {
			throw new RuntimeException (type_name() + ": Cannot convert to IgesDate [" + toString() + "]");
		}

	}




	// IGES data value for reserved fields.

	public static class IgesReserved extends IgesData {

		// Construct defaulted value.

		public IgesReserved () {
			this.f_defaulted = true;
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return "";
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesReserved";
		}

	}




	// IGES integer.

	public static class IgesInteger extends IgesData {
	
		// The value.

		protected int value;

		public int get_value () {
			return value;
		}

		// Construct defaulted value.

		public IgesInteger () {
			this.f_defaulted = true;
			this.value = 0;
		}

		// Construct value.

		public IgesInteger (int value) {
			if (!( G_INT_MIN <= value && value <= G_INT_MAX )) {
				throw new RuntimeException ("IgesInteger: Value out of range: " + value);
			}
			this.f_defaulted = false;
			this.value = value;
		}

		//public IgesInteger (IgesBoolean value) {
		//	if (value.is_defaulted()) {
		//		this.f_defaulted = true;
		//		this.value = 0;
		//	} else {
		//		this.f_defaulted = false;
		//		this.value = (value.get_value() ? 1 : 0);
		//	}
		//}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return String.valueOf (value);
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesInteger";
		}

		@Override
		public IgesInteger as_integer () {
			return this;
		}

		@Override
		public IgesBoolean as_boolean () {
			if (f_defaulted) {
				return new IgesBoolean ();
			} else if (value == 0) {
				return new IgesBoolean (false);
			} else if (value == 1) {
				return new IgesBoolean (true);
			}
			return super.as_boolean ();
		}

		@Override
		public IgesIntegerOrPointer as_integer_or_pointer () {
			if (f_defaulted) {
				return new IgesIntegerOrPointer ();
			} else if (value >= 0) {
				return new IgesIntegerOrPointer (this);
			}
			return super.as_integer_or_pointer ();
		}

	}




	// IGES real.

	public static abstract class IgesReal extends IgesData {
	
		// The value.

		protected double value;

		public double get_value () {
			return value;
		}

	}




	// IGES real single precision.
	// Same as double precision except for the string representation.

	public static class IgesFloat extends IgesReal {

		// Construct defaulted value.

		public IgesFloat () {
			this.f_defaulted = true;
			this.value = 0.0;
		}

		// Construct value.

		public IgesFloat (double value) {
			if (!( -G_FLOAT_MAX <= value && value <= G_FLOAT_MAX )) {
				throw new RuntimeException ("IgesFloat: Value out of range: " + value);
			}
			this.f_defaulted = false;
			if (-G_FLOAT_MIN <= value && value <= G_FLOAT_MIN) {
				this.value = 0.0;
			} else {
				this.value = value;
			}
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			String result = String.format ("%.6G", value);

			// The following step is needed because IGES requires that the decimal point or
			// exponent (or both) be present, but the Java %G format sometimes produces neither
			if (!( result.contains (".") )) {
				result = String.format ("%.5E", value);
			}

			//return result;

			// This removes trailing zeros, but leaving at least one digit after the decimal point
			return result
					.replaceFirst ("(?<=\\.\\d{0,15}[1-9])0+\\z", "")
					.replaceFirst ("(?<=\\.0)0+\\z", "")
					.replaceFirst ("(?<=\\.\\d{0,15}[1-9])0+(?=E)", "")
					.replaceFirst ("(?<=\\.0)0+(?=E)", "");
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesFloat";
		}

		@Override
		public IgesReal as_real () {
			return this;
		}

		@Override
		public IgesFloat as_float () {
			return this;
		}

	}




	// IGES real double precision.

	public static class IgesDouble extends IgesReal {

		// Construct defaulted value.

		public IgesDouble () {
			this.f_defaulted = true;
			this.value = 0.0;
		}

		// Construct value.

		public IgesDouble (double value) {
			if (!( -G_DOUBLE_MAX <= value && value <= G_DOUBLE_MAX )) {
				throw new RuntimeException ("IgesDouble: Value out of range: " + value);
			}
			this.f_defaulted = false;
			if (-G_DOUBLE_MIN <= value && value <= G_DOUBLE_MIN) {
				this.value = 0.0;
			} else {
				this.value = value;
			}
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			//return String.format ("%.14E", value).replace ("E", "D");

			// This removes trailing zeros, but leaving at least one digit after the decimal point
			return String.format ("%.14E", value)
					.replaceFirst ("(?<=\\.\\d{0,15}[1-9])0+(?=E)", "")
					.replaceFirst ("(?<=\\.0)0+(?=E)", "")
					.replace ("E", "D");
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesDouble";
		}

		@Override
		public IgesReal as_real () {
			return this;
		}

		@Override
		public IgesDouble as_double () {
			return this;
		}

	}




	// IGES string-valued data.
	// Non-defaulted strings must have non-zero length.

	public static abstract class IgesStringBase extends IgesData {
	
		// The value.

		protected String value;

		public String get_value () {
			return value;
		}

		public int get_num_chars () {
			return (f_defaulted ? 0 : value.length());
		}

	}




	// IGES string.
	// Non-defaulted strings must have non-zero length.

	public static class IgesString extends IgesStringBase {

		// Construct defaulted value.

		public IgesString () {
			this.f_defaulted = true;
			this.value = "";
		}

		// Construct value.

		public IgesString (String value) {
			if (!( value != null )) {
				throw new RuntimeException ("IgesString: Value out of range: [" + value + "]");
			}
			if (value.length() == 0) {
				this.f_defaulted = true;
				this.value = "";
			} else {
				this.f_defaulted = false;
				this.value = value;
			}
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return value.length() + "H" + value;
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesString";
		}

		@Override
		public IgesStringBase as_string_base () {
			return this;
		}

		@Override
		public IgesString as_string () {
			return this;
		}

		@Override
		public IgesLiteralString as_literal_string () {
			if (f_defaulted) {
				return new IgesLiteralString ();
			}
			return new IgesLiteralString (value);
		}

	}




	// IGES pointer.
	// Pointer value must be non-negative (even though IGES allows negative pointer values).

	public static class IgesPointer extends IgesData {
	
		// The value.

		protected int value;

		public int get_value () {
			return value;
		}

		public boolean is_null () {
			return (f_defaulted || value == 0);
		}

		// Construct defaulted value.

		public IgesPointer () {
			this.f_defaulted = true;
			this.value = 0;
		}

		// Construct value.

		public IgesPointer (int value) {
			if (!( 0 <= value && value <= G_POINTER_MAX )) {
				throw new RuntimeException ("IgesPointer: Value out of range: " + value);
			}
			this.f_defaulted = false;
			this.value = value;
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return String.valueOf (value);
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesPointer";
		}

		@Override
		public IgesPointer as_pointer () {
			return this;
		}

		@Override
		public IgesIntegerOrPointer as_integer_or_pointer () {
			return new IgesIntegerOrPointer (this);
		}

	}




	// IGES literal string.
	// Strings must have non-zero length.

	public static class IgesLiteralString extends IgesStringBase {

		// Construct defaulted value.

		public IgesLiteralString () {
			this.f_defaulted = true;
			this.value = "";
		}

		// Construct value.

		public IgesLiteralString (String value) {
			if (!( value != null )) {
				throw new RuntimeException ("IgesLiteralString: Value out of range: [" + value + "]");
			}
			if (value.length() == 0) {
				this.f_defaulted = true;
				this.value = "";
			} else {
				this.f_defaulted = false;
				this.value = value;
			}
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return value;
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesLiteralString";
		}

		@Override
		public IgesStringBase as_string_base () {
			return this;
		}

		@Override
		public IgesString as_string () {
			if (f_defaulted) {
				return new IgesString ();
			}
			return new IgesString (value);
		}

		@Override
		public IgesLiteralString as_literal_string () {
			return this;
		}

	}




	// IGES boolean (logical).

	public static class IgesBoolean extends IgesData {
	
		// The value.

		protected boolean value;

		public boolean get_value () {
			return value;
		}

		// Construct defaulted value.

		public IgesBoolean () {
			this.f_defaulted = true;
			this.value = false;
		}

		// Construct value.

		public IgesBoolean (boolean value) {
			this.f_defaulted = false;
			this.value = value;
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return (value ? "1" : "0");
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesBoolean";
		}

		@Override
		public IgesInteger as_integer () {
			if (f_defaulted) {
				return new IgesInteger ();
			}
			return new IgesInteger (value ? 1 : 0);
		}

		@Override
		public IgesBoolean as_boolean () {
			return this;
		}

	}




	// IGES integer or pointer.
	// Integers are positive (and maybe zero), pointers are negative.

	public static class IgesIntegerOrPointer extends IgesData {
	
		// The value.

		protected int value;

		public int get_value () {
			return value;
		}

		public int get_abs_value () {
			return Math.abs (value);
		}

		public boolean is_pointer () {
			return value < 0;
		}

		public boolean is_null () {
			return (f_defaulted || value == 0);
		}

		// Construct defaulted value.

		public IgesIntegerOrPointer () {
			this.f_defaulted = true;
			this.value = 0;
		}

		// Construct value.

		public IgesIntegerOrPointer (int value) {
			if (!( G_POINTER_MIN <= value && value <= G_INT_MAX )) {
				throw new RuntimeException ("IgesIntegerOrPointer: Value out of range: " + value);
			}
			this.f_defaulted = false;
			this.value = value;
		}

		public IgesIntegerOrPointer (IgesInteger value) {
			if (value.is_defaulted()) {
				this.f_defaulted = true;
				this.value = 0;
			} else {
				this.f_defaulted = false;
				this.value = value.get_value();
				if (!( 0 <= this.value && this.value <= G_INT_MAX )) {
					throw new RuntimeException ("IgesIntegerOrPointer: Integer value out of range: " + this.value);
				}
			}
		}

		public IgesIntegerOrPointer (IgesPointer value) {
			if (value.is_defaulted()) {
				this.f_defaulted = true;
				this.value = 0;
			} else {
				this.f_defaulted = false;
				this.value = - value.get_value();
				if (!( G_POINTER_MIN <= this.value && this.value <= 0 )) {
					throw new RuntimeException ("IgesIntegerOrPointer: Pointer value out of range: " + this.value);
				}
			}
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			return String.valueOf (value);
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesIntegerOrPointer";
		}

		@Override
		public IgesInteger as_integer () {
			if (f_defaulted) {
				return new IgesInteger ();
			} else if (value >= 0) {
				return new IgesInteger (value);
			}
			return super.as_integer ();
		}

		@Override
		public IgesPointer as_pointer () {
			if (f_defaulted) {
				return new IgesPointer ();
			} else if (value <= 0) {
				return new IgesPointer (- value);
			}
			return super.as_pointer ();
		}

		@Override
		public IgesIntegerOrPointer as_integer_or_pointer () {
			return this;
		}

	}




	// IGES date.

	public static class IgesDate extends IgesData {
	
		// The value.

		protected long value;

		public long get_value () {
			return value;
		}

		// Construct defaulted value.

		public IgesDate () {
			this.f_defaulted = true;
			this.value = 0L;
		}

		// Construct value.

		public IgesDate (long value) {
			this.f_defaulted = false;
			this.value = value;
		}

		// String representation.

		@Override
		protected String iges_string_rep () {
			SimpleDateFormat fmt = new SimpleDateFormat ("'15H'yyyyMMdd.HHmmss");
			fmt.setTimeZone (TimeZone.getTimeZone ("UTC"));
			return fmt.format (new Date (value));
		}

		// Conversion functions.

		@Override
		public String type_name () {
			return "IgesDate";
		}

		public IgesDate as_date () {
			return this;
		}

	}




	//----- IGES data container -----




	// List of data values.
	//
	// Conversions below are supported.
	// Therefore, for directory entries, a type on the left can be used when any of the types on the right are required.
	// IgesInteger -> IgesBoolean, IgesIntegerOrPointer
	// IgesFloat -> IgesReal
	// IgesDouble -> IgesReal
	// IgesString -> IgesStringBase, IgesLiteralString
	// IgesPointer -> IgesIntegerOrPointer
	// IgesLiteralString -> IgesStringBase, IgesString
	// IgesBoolean -> IgesInteger
	// IgesIntegerOrPointer -> IgesInteger, IgesPointer

	public static class IgesDataList extends ArrayList<IgesData> {

		//----- Addition methods -----

		// Add an integer.

		public void add_integer () {
			add (new IgesInteger ());
			return;
		}

		public void add_integer (int value) {
			add (new IgesInteger (value));
			return;
		}

		public void add_integer (IgesData value) {
			add (value.as_integer());
			return;
		}

		// Add a single-precision real.

		public void add_float () {
			add (new IgesFloat ());
			return;
		}

		public void add_float (double value) {
			add (new IgesFloat (value));
			return;
		}

		public void add_float (IgesData value) {
			add (value.as_float());
			return;
		}

		// Add a double-precision real.

		public void add_double () {
			add (new IgesDouble ());
			return;
		}

		public void add_double (double value) {
			add (new IgesDouble (value));
			return;
		}

		public void add_double (IgesData value) {
			add (value.as_double());
			return;
		}

		// Add a string.

		public void add_string () {
			add (new IgesString ());
			return;
		}

		public void add_string (String value) {
			add (new IgesString (value));
			return;
		}

		public void add_string (IgesData value) {
			add (value.as_string());
			return;
		}

		// Add a pointer.

		public void add_pointer () {
			add (new IgesPointer ());
			return;
		}

		//public void add_pointer (IgesData value) {
		//	if (value == null) {
		//		add (new IgesPointer());
		//	} else {
		//		add (value.as_pointer());
		//	}
		//	return;
		//}

		public void add_pointer (Object value) {
			if (value == null) {
				add (new IgesPointer ());
			} else if (value instanceof Integer) {
				if (!( ((Integer)value).intValue() == 0 )) {
					throw new IllegalArgumentException ("IgesDataList.add_pointer: Nonzero integer value: " + ((Integer)value).intValue());
				}
				add (new IgesPointer (0));
			} else if (value instanceof IgesData) {
				add (((IgesData)value).as_pointer());
			} else {
				throw new IllegalArgumentException ("IgesDataList.add_pointer: Invalid data type");
			}
			return;
		}

		// Add a literal string.

		public void add_literal_string () {
			add (new IgesLiteralString ());
			return;
		}

		public void add_literal_string (String value) {
			add (new IgesLiteralString (value));
			return;
		}

		public void add_literal_string (IgesData value) {
			add (value.as_literal_string());
			return;
		}

		// Add a boolean.

		public void add_boolean () {
			add (new IgesBoolean ());
			return;
		}

		public void add_boolean (boolean value) {
			add (new IgesBoolean (value));
			return;
		}

		public void add_boolean (IgesData value) {
			add (value.as_boolean());
			return;
		}

		// Add an integer or pointer.

		public void add_integer_or_pointer () {
			add (new IgesIntegerOrPointer ());
			return;
		}

		public void add_integer_or_pointer (int value) {
			add (new IgesIntegerOrPointer (new IgesInteger (value)));
			return;
		}

		//public void add_integer_or_pointer (IgesData value) {
		//	add (value.as_integer_or_pointer());
		//	return;
		//}

		public void add_integer_or_pointer (Object value) {
			if (value == null) {
				add (new IgesIntegerOrPointer ());
			} else if (value instanceof Integer) {
				add (new IgesIntegerOrPointer (new IgesInteger (((Integer)value).intValue())));
			} else if (value instanceof IgesData) {
				add (((IgesData)value).as_integer_or_pointer());
			} else {
				throw new IllegalArgumentException ("IgesDataList.add_integer_or_pointer: Invalid data type");
			}
			return;
		}

		// Add a date.

		public void add_date () {
			add (new IgesDate ());
			return;
		}

		public void add_date (long value) {
			add (new IgesDate (value));
			return;
		}

		public void add_date (IgesData value) {
			add (value.as_date());
			return;
		}

		//----- Extended addition methods -----

		// Add an array of double, from index begin to end-1.

		public void add_double_array (double [] value, int begin, int end) {
			for (int i = begin; i < end; ++i) {
				add (new IgesDouble (value[i]));
			}
			return;
		}

		// Add a 2D array of double, C-style (last index varies most rapidly), for the given index ranges.

		public void add_double_2d_c_array (double[][] value, int begin_1, int end_1, int begin_2, int end_2) {
			for (int i = begin_1; i < end_1; ++i) {
				for (int j = begin_2; j < end_2; ++j) {
					add (new IgesDouble (value[i][j]));
				}
			}
			return;
		}

		// Add a 2D array of double, Fortran-style (first index varies most rapidly), for the given index ranges.

		public void add_double_2d_f_array (double[][] value, int begin_1, int end_1, int begin_2, int end_2) {
			for (int j = begin_2; j < end_2; ++j) {
				for (int i = begin_1; i < end_1; ++i) {
					add (new IgesDouble (value[i][j]));
				}
			}
			return;
		}

		// Add a double, repeating the given number of times.

		public void add_double_repeated (double value, int count) {
			for (int i = 0; i < count; ++i) {
				add (new IgesDouble (value));
			}
			return;
		}

		//----- Retrieval methods -----

		// Get an integer.

		public IgesInteger get_integer (int i) {
			return get(i).as_integer();
		}

		// Get a real.

		public IgesReal get_real (int i) {
			return get(i).as_real();
		}

		// Get a string.

		public IgesString get_string (int i) {
			return get(i).as_string();
		}

		// Get a pointer.

		public IgesPointer get_pointer (int i) {
			return get(i).as_pointer();
		}

		// Get a literal string.

		public IgesLiteralString get_literal_string (int i) {
			return get(i).as_literal_string();
		}

		// Get a boolean.

		public IgesBoolean get_boolean (int i) {
			return get(i).as_boolean();
		}

		// Get an integer or pointer.

		public IgesIntegerOrPointer get_integer_or_pointer (int i) {
			return get(i).as_integer_or_pointer();
		}

		// Get a date.

		public IgesDate get_date (int i) {
			return get(i).as_date();
		}




		//----- Special functions -----




		// Find a range that fits in a given number of columns.
		// Return value is the end+1 index of a range, starting at begin, that fits in the
		// given number of columns, allowing one extra column for each data item.
		// If the entire remaining list fits, then the return value equals size().

		public int find_range_in (int begin, int num_cols) {
			int index = begin;
			int count = get(index).toString().length() + 1;
			if (count > num_cols) {
				throw new RuntimeException ("IgesDataList.find_range_in: Data item too long: " + get(index).toString());
			}
			for (++index; index < size(); ++index) {
				count += (get(index).toString().length() + 1);
				if (count > num_cols) {
					break;
				}
			}
			return index;
		}




		// Set up global parameters for a common case.
		// Parameters:
		//  product_id = Product identification.
		//  file_name = Filename, to be embedded in the file.
		//  units_flag = Unit, UFLAG_XXXXX.
		//  line_thickness_count = Number of different line thicknesses, >= 1.
		//  max_line_thickness = Maximum line thickness, in the selected units.
		//  min_resolution = Smallest distance between coordinates that is considered to be nonzero.
		//  max_coordinate = Maximum absolute value of any coordinate, or 0.0 if unknown.

		public void set_global_params (
			String product_id,
			String file_name,
			int units_flag,
			int line_thickness_count,
			double max_line_thickness,
			double min_resolution,
			double max_coordinate) {
		
			// Delimiters

			add_string (PARAM_SEP);
			add_string (REC_TERM);

			// Product identification from sender

			add_string (product_id);

			// File name

			add_string (file_name);

			// Native system ID

			add_string (SYSTEM_ID);

			// Preprocessor version which created this file

			add_string (SYSTEM_ID);

			// Numeric characteristics

			add_integer (G_INT_BITS);
			add_integer (G_FLOAT_EXP);
			add_integer (G_FLOAT_DIGITS);
			add_integer (G_DOUBLE_EXP);
			add_integer (G_DOUBLE_DIGITS);

			// Product identification for receiver

			add_string ();

			// Model space scale

			add_float (1.0);

			// Units flag and name

			add_integer (units_flag);

			switch (units_flag) {
				default:
					throw new RuntimeException ("IgesDataList.set_global_params: Invalid units flag: " + units_flag);
				case UFLAG_INCH:		add_string (UNAME_INCH); break;
				case UFLAG_MILLIMETER:	add_string (UNAME_MILLIMETER); break;
				case UFLAG_FOOT:		add_string (UNAME_FOOT); break;
				case UFLAG_MILE:		add_string (UNAME_MILE); break;
				case UFLAG_METER:		add_string (UNAME_METER); break;
				case UFLAG_KILOMETER:	add_string (UNAME_KILOMETER); break;
				case UFLAG_MIL:			add_string (UNAME_MIL); break;
				case UFLAG_MICRON:		add_string (UNAME_MICRON); break;
				case UFLAG_CENTIMETER:	add_string (UNAME_CENTIMETER); break;
				case UFLAG_MICROINCH:	add_string (UNAME_MICROINCH); break;
			}

			// Number of line weight gradations

			add_integer (line_thickness_count);

			// Maximum line thickness

			add_float (max_line_thickness);

			// Date and time of file generation

			add_date (System.currentTimeMillis());

			// Minimum resolution

			add_double (min_resolution);

			// Maximum absolute coordinate value, or 0.0 if unknown

			add_double (max_coordinate);

			// Author and authors' organization

			add_string ();
			add_string ();

			// Version flag

			add_integer (VERSION_FLAG);

			// Drafting standard flag

			add_integer (0);

			// Date model was last modified, default if unspecified

			//add_date ();

			// Application protocol or subset identifier, default if unspecified

			//add_string ();

			return;
		}




		// Set directory entry parameters.
		// Parameters:
		//  structure = Negated pointer to schema definition, or 0 (default).
		//  line_pattern = Line font pattern number (LINE_PATTERN_XXXX), or negated pointer to line font definition, or 0 (default).
		//  level = Level number, or negated pointer to level property entity, or 0 (default).
		//  view = Pointer to view entity, or 0 (default).
		//  xform_matrix = Pointer to transformation matrix entity, or 0 (default).
		//  label_assoc = Pointer to label display associativity, or 0 (default).
		//  status_blank = Blank status (ESTAT_BLANK_XXXX);
		//  status_subord = Subordinate status (ESTAT_SUBORD_XXXX).
		//  status_use = Use status (ESTAT_USE_XXXX).
		//  status_hier = Hierarchy status (ESTAT_HIER_XXXX);
		//  line_thickness = Line thickness number, or 0 for receiving system default.
		//  color = Color number (COLOR_XXXX), or negated pointer to color definition entiry, or 0 (default) for none.
		//  form = Form number, or 0 (default).
		//  label = Entity label, or empty string (default).
		//  subscript = Entity subscript number.
		//
		// For structure, line_pattern, level, and color: The argument can be:
		//  null, which yields a defaulted (blank) pointer.
		//  Integer, which must be non-negative and yields an integer or zero value (must be zero for structure).
		//  IgesInteger, which must be non-negative and yields an integer or zero value (must be zero for structure).
		//  IgesPointer, which yields a pointer.
		//  IgesIntegerOrPointer, which yields a non-negative integer or a pointer.
		//
		// For view, xform_matrix, and label_assoc: The argument can be:
		//  null, which yields a defaulted (blank) pointer.
		//  Integer, which must be zero and yields an explicitly zero pointer.
		//  IgesPointer, which yields a pointer.
		//  IgesIntegerOrPointer, which must contain a pointer and yields a pointer.

		public void set_dir_entry_params (
			Object structure,
			Object line_pattern,
			Object level,
			Object view,
			Object xform_matrix,
			Object label_assoc,
			int status_blank,
			int status_subord,
			int status_use,
			int status_hier,
			int line_thickness,
			Object color,
			int form,
			String label,
			int subscript) {

			// Structure

			add_integer_or_pointer (structure);

			// Line font pattern

			add_integer_or_pointer (line_pattern);

			// Level

			add_integer_or_pointer (level);

			// View

			add_pointer (view);

			// Transformation matrix

			add_pointer (xform_matrix);

			// Label display associativity

			add_pointer (label_assoc);

			// Status values

			add_integer (status_blank);

			add_integer (status_subord);

			add_integer (status_use);

			add_integer (status_hier);

			// Line thickness

			add_integer (line_thickness);

			// Color

			add_integer_or_pointer (color);

			// Form

			add_integer (form);

			// Label

			add_literal_string (label);

			// Subscript

			add_integer (subscript);
			
			return;
		}




		// Set parameters for a B-spline surface.
		// Parameters:
		//  bss_k1 = Number of control points - 1, for the first array index.
		//  bss_k2 = Number of control points - 1, for the second array index.
		//  bss_m1 = Polynomial degree, for the first array index.
		//  bss_m2 = Polynomial degree, for the second array index.
		//  bss_closed1 = True if the surface is closed, for the first array index.
		//  bss_closed2 = True if the surface is closed, for the second array index.
		//  bss_periodic1 = True if the surface is periodic, for the first array index.
		//  bss_periodic2 = True if the surface is periodic, for the second array index.
		//  bss_s = Knots, for the first array index, length = bss_k1 + bss_m1 + 2.
		//  bss_t = Knots, for the second array index, length = bss_k2 + bss_m2 + 2.
		//  bss_w = Weights, or null, dimension bss_w[bss_k1 + 1][bss_k2 + 1].
		//  bss_x = Control point x-coordinates, dimension bss_x[bss_k1 + 1][bss_k2 + 1].
		//  bss_y = Control point y-coordinates, dimension bss_y[bss_k1 + 1][bss_k2 + 1].
		//  bss_z = Control point z-coordinates, dimension bss_z[bss_k1 + 1][bss_k2 + 1].
		//  bss_u0 = Starting parameter value, for the first array index, must be >= first active knot = bss_s[bss_m1].
		//  bss_u1 = Ending parameter value, for the first array index, must be <= last active knot = bss_s[bss_k1 + 1].
		//  bss_v0 = Starting parameter value, for the second array index, must be >= first active knot = bss_t[bss_m2].
		//  bss_v1 = Ending parameter value, for the second array index, must be <= last active knot = bss_t[bss_k2 + 1].
		//
		// Note: The first and last M1 or M2 knots are inactive.  The number of active knots
		// in the first and second directions are N1 + 1 and N2 + 1, where
		//  N1 = K1 - M1 + 1
		//  N2 = K2 - M2 + 1

		public void set_bss_params (
			int bss_k1,
			int bss_k2,
			int bss_m1,
			int bss_m2,
			boolean bss_closed1,
			boolean bss_closed2,
			boolean bss_periodic1,
			boolean bss_periodic2,
			double[] bss_s,
			double[] bss_t,
			double[][] bss_w,
			double[][] bss_x,
			double[][] bss_y,
			double[][] bss_z,
			double bss_u0,
			double bss_u1,
			double bss_v0,
			double bss_v1) {

			// Entity type

			add_integer (ETYPE_B_SPLINE_SURFACE);

			// Number of control points - 1

			add_integer (bss_k1);
			add_integer (bss_k2);

			// Polynomial degree

			add_integer (bss_m1);
			add_integer (bss_m2);

			// Closed flags

			add_boolean (bss_closed1);
			add_boolean (bss_closed2);

			// Flag indicating if surface is polynomial (has all weights equal)

			boolean f_polynomial = true;
			if (bss_w != null) {
				for (int i2 = 0; i2 <= bss_k2; ++i2) {
					for (int i1 = 0; i1 <= bss_k1; ++i1) {
						if (bss_w[i1][i2] != bss_w[0][0]) {
							f_polynomial = false;
						}
					}
				}
			}
			add_boolean (f_polynomial);

			// Periodic flags

			add_boolean (bss_periodic1);
			add_boolean (bss_periodic2);

			// Knots

			if (!( bss_s.length == bss_k1 + bss_m1 + 2 )) {
				throw new IllegalArgumentException ("set_bss_params: Bad length for knots bss_s: " + bss_s.length + ", expecting " + (bss_k1 + bss_m1 + 2));
			}

			for (int i1 = 0; i1 <= bss_k1 + bss_m1 + 1; ++i1) {
				add_double (bss_s[i1]);

				if (i1 > 0) {
					if (!( bss_s[i1] >= bss_s[i1 - 1] )) {
						throw new IllegalArgumentException ("set_bss_params: Out-of-order knot bss_s[" + i1 + "]: " + bss_s[i1]);
					}
				}
			}

			if (!( bss_t.length == bss_k2 + bss_m2 + 2 )) {
				throw new IllegalArgumentException ("set_bss_params: Bad length for knots bss_t: " + bss_t.length + ", expecting " + (bss_k2 + bss_m2 + 2));
			}

			for (int i2 = 0; i2 <= bss_k2 + bss_m2 + 1; ++i2) {
				add_double (bss_t[i2]);

				if (i2 > 0) {
					if (!( bss_t[i2] >= bss_t[i2 - 1] )) {
						throw new IllegalArgumentException ("set_bss_params: Out-of-order knot bss_t[" + i2 + "]: " + bss_t[i2]);
					}
				}
			}

			// Weights

			if (bss_w != null) {

				if (!( bss_w.length == bss_k1 + 1 )) {
					throw new IllegalArgumentException ("set_bss_params: Bad length for weights bss_w: " + bss_w.length + ", expecting " + (bss_k1 + 1));
				}
				for (int i1 = 0; i1 <= bss_k1; ++i1) {
					if (!( bss_w[i1].length == bss_k2 + 1 )) {
						throw new IllegalArgumentException ("set_bss_params: Bad length for weights bss_w[" + i1 + "]: " + bss_w[i1].length + ", expecting " + (bss_k2 + 1));
					}
				}

				for (int i2 = 0; i2 <= bss_k2; ++i2) {
					for (int i1 = 0; i1 <= bss_k1; ++i1) {
						add_double (bss_w[i1][i2]);

						if (!( bss_w[i1][i2] > 0.0 )) {
							throw new IllegalArgumentException ("set_bss_params: Non-positive weight bss_w[" + i1 + "][" + i2 + "]: " + bss_w[i1][i2]);
						}
					}
				}
			}
			else {
				for (int i2 = 0; i2 <= bss_k2; ++i2) {
					for (int i1 = 0; i1 <= bss_k1; ++i1) {
						add_double (1.0);
					}
				}
			}

			// Control points

			if (!( bss_x.length == bss_k1 + 1 )) {
				throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_x: " + bss_x.length + ", expecting " + (bss_k1 + 1));
			}
			for (int i1 = 0; i1 <= bss_k1; ++i1) {
				if (!( bss_x[i1].length == bss_k2 + 1 )) {
					throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_x[" + i1 + "]: " + bss_x[i1].length + ", expecting " + (bss_k2 + 1));
				}
			}

			if (!( bss_y.length == bss_k1 + 1 )) {
				throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_y: " + bss_y.length + ", expecting " + (bss_k1 + 1));
			}
			for (int i1 = 0; i1 <= bss_k1; ++i1) {
				if (!( bss_y[i1].length == bss_k2 + 1 )) {
					throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_y[" + i1 + "]: " + bss_y[i1].length + ", expecting " + (bss_k2 + 1));
				}
			}

			if (!( bss_z.length == bss_k1 + 1 )) {
				throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_z: " + bss_z.length + ", expecting " + (bss_k1 + 1));
			}
			for (int i1 = 0; i1 <= bss_k1; ++i1) {
				if (!( bss_z[i1].length == bss_k2 + 1 )) {
					throw new IllegalArgumentException ("set_bss_params: Bad length for control points bss_z[" + i1 + "]: " + bss_z[i1].length + ", expecting " + (bss_k2 + 1));
				}
			}

			for (int i2 = 0; i2 <= bss_k2; ++i2) {
				for (int i1 = 0; i1 <= bss_k1; ++i1) {
					add_double (bss_x[i1][i2]);
					add_double (bss_y[i1][i2]);
					add_double (bss_z[i1][i2]);
				}
			}

			// Parameter ranges

			add_double (bss_u0);
			add_double (bss_u1);

			if (!( bss_s[bss_m1] <= bss_u0 && bss_u0 < bss_u1 && bss_u1 <= bss_s[bss_k1 + 1] )) {
				throw new IllegalArgumentException ("set_bss_params: Invalid parameter range (bss_u0, bss_u1): (" + bss_u0 + ", " + bss_u1 + "), valid range (" + bss_s[bss_m1] + ", " + bss_s[bss_k1 + 1] + ")");
			}

			add_double (bss_v0);
			add_double (bss_v1);

			if (!( bss_t[bss_m2] <= bss_v0 && bss_v0 < bss_v1 && bss_v1 <= bss_t[bss_k2 + 1] )) {
				throw new IllegalArgumentException ("set_bss_params: Invalid parameter range (bss_v0, bss_v1): (" + bss_v0 + ", " + bss_v1 + "), valid range (" + bss_t[bss_m2] + ", " + bss_t[bss_k2 + 1] + ")");
			}

			return;
		}

	}




	//----- IGES cards -----




	// Base class for all IGES cards.
	// All IGES cards are immutable.

	public static abstract class CardBase {

		// Letter code for this card (LC_XXXX).

		protected String letter_code;

		public String get_letter_code () {
			return letter_code;
		}

		// Sequence number for this card.

		protected IgesPointer seq_num;

		public IgesPointer get_seq_num () {
			return seq_num;
		}

		// End-of-record flag.

		protected boolean eor;

		public boolean is_eor () {
			return eor;
		}

		// Constructor.

		protected CardBase (String letter_code, IgesPointer seq_num, boolean eor) {
			this.letter_code = letter_code;
			this.seq_num = seq_num;
			this.eor = eor;
		}

		// Hook method used by subclasses to return the first 72 columns.

		protected abstract String hook_72_cols ();

		// Our string representation is the card image, 80 characters.
		// The return from hook_72_cols is padded if necessary to 72 columns.

		@Override
		public String toString () {
			String result = String.format ("%-72s%1s%7s", hook_72_cols(), letter_code, seq_num);
			if (result.length() != 80) {
				throw new RuntimeException ("CardBase: Card length error: [" + result + "]");
			}
			return result;
		}

	}




	// Card for start record.

	public static class CardStart extends CardBase {

		// Text.

		protected IgesLiteralString text;

		public IgesLiteralString get_text () {
			return text;
		}

		// Constructor.
		// Text should not exceed CW_START = 72 characters.

		public CardStart (IgesPointer seq_num, boolean eor, IgesLiteralString text) {
			super (LC_START, seq_num, eor);
			this.text = text;

			if (!( text.get_num_chars() <= CW_START )) {
				throw new IllegalArgumentException ("CardStart: Invalid text: " + text.get_value());
			}
		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			return text.toString();
		}

	}




	// Card for global record.

	public static class CardGlobal extends CardBase {

		// Parameters.

		protected IgesData[] params;

		public int get_param_count () {
			return params.length;
		}

		public IgesData get_param (int i) {
			return params[i];
		}

		// Constructor.
		// Total length of parameters, including 1 char per parameter for separators, should not exceed CW_GLOBAL = 72 characters.

		public CardGlobal (IgesPointer seq_num, boolean eor, List<IgesData> params) {
			super (LC_GLOBAL, seq_num, eor);
			this.params = params.toArray (new IgesData[0]);
		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			StringBuilder sb = new StringBuilder();
			int n = params.length;
			for (int i = 0; i < n; ++i) {
				sb.append (params[i].toString());
				sb.append ((eor && (i+1) == n) ? REC_TERM : PARAM_SEP);
			}
			return sb.toString();
		}

	}




	// Card for directory entry record.

	public static abstract class CardDirEntry extends CardBase {

		// Constructor.

		public CardDirEntry (IgesPointer seq_num, boolean eor) {
			super (LC_DIR_ENTRY, seq_num, eor);
		}

	}




	// Card for directory entry 1 record.

	public static class CardDirEntry1 extends CardDirEntry {

		// Entity type.

		protected IgesInteger entity_type;

		public IgesInteger get_entity_type () {
			return entity_type;
		}

		// Parameter data (pointer to first line of parameter data record).

		protected IgesPointer param_data_ptr;

		public IgesPointer get_param_data_ptr () {
			return param_data_ptr;
		}

		// Structure, can be zero (default).

		protected IgesIntegerOrPointer structure;

		public IgesIntegerOrPointer get_structure () {
			return structure;
		}

		// Line font pattern (line style), can be zero (default) to select default style.

		protected IgesIntegerOrPointer line_font;

		public IgesIntegerOrPointer get_line_font () {
			return line_font;
		}

		// Level, can be zero (default).

		protected IgesIntegerOrPointer level;

		public IgesIntegerOrPointer get_level () {
			return level;
		}

		// View, can be zero (default) if visible with same characteristic in all views.

		protected IgesPointer view;

		public IgesPointer get_view () {
			return view;
		}

		// Transformation matrix, can be zero (default) if none.

		protected IgesPointer xform_matrix;

		public IgesPointer get_xform_matrix () {
			return xform_matrix;
		}

		// Label display association, can be zero (default) for standard display in all views.

		protected IgesPointer label_disp;

		public IgesPointer get_label_disp () {
			return label_disp;
		}

		// Entity status, ESTAT_XXXX (4 fields), required values (no defaulting).

		protected IgesInteger entity_status_blank;
		protected IgesInteger entity_status_subord;
		protected IgesInteger entity_status_use;
		protected IgesInteger entity_status_hier;

		public IgesInteger get_entity_status_blank () {
			return entity_status_blank;
		}

		public IgesInteger get_entity_status_subord () {
			return entity_status_subord;
		}

		public IgesInteger get_entity_status_use () {
			return entity_status_use;
		}

		public IgesInteger get_entity_status_hier () {
			return entity_status_hier;
		}

		public IgesInteger get_entity_status () {
			return new IgesInteger (
				entity_status_blank.get_value() * 1000000
				+ entity_status_subord.get_value() * 10000
				+ entity_status_use.get_value() * 100
				+ entity_status_hier.get_value()
			);
		}

		// Constructor.

		public CardDirEntry1 (IgesPointer seq_num,
				IgesInteger entity_type,
				IgesPointer param_data_ptr,
				IgesIntegerOrPointer structure,
				IgesIntegerOrPointer line_font,
				IgesIntegerOrPointer level,
				IgesPointer view,
				IgesPointer xform_matrix,
				IgesPointer label_disp,
				IgesInteger entity_status_blank,
				IgesInteger entity_status_subord,
				IgesInteger entity_status_use,
				IgesInteger entity_status_hier) {
			super (seq_num, false);
			this.entity_type = entity_type;
			this.param_data_ptr = param_data_ptr;
			this.structure = structure;
			this.line_font = line_font;
			this.level = level;
			this.view = view;
			this.xform_matrix = xform_matrix;
			this.label_disp = label_disp;
			this.entity_status_blank = entity_status_blank;
			this.entity_status_subord = entity_status_subord;
			this.entity_status_use = entity_status_use;
			this.entity_status_hier = entity_status_hier;

			if (!( structure.get_value() <= 0 )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid structure: " + structure.get_value());
			}

			if (!( line_font.get_value() <= LINE_PATTERN_DOTTED )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid line_font: " + line_font.get_value());
			}

			if (!( entity_status_blank.get_value() >= 0 && entity_status_blank.get_value() <= ESTAT_BLANK_BLANKED )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid entity_status_blank: " + entity_status_blank.get_value());
			}

			if (!( entity_status_subord.get_value() >= 0 && entity_status_subord.get_value() <= ESTAT_SUBORD_BOTH )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid entity_status_subord: " + entity_status_subord.get_value());
			}

			if (!( entity_status_use.get_value() >= 0 && entity_status_use.get_value() <= ESTAT_USE_CONSTRUCTION )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid entity_status_use: " + entity_status_use.get_value());
			}

			if (!( entity_status_hier.get_value() >= 0 && entity_status_hier.get_value() <= ESTAT_HIER_HIERARCHY )) {
				throw new IllegalArgumentException ("CardDirEntry1: Invalid entity_status_hier: " + entity_status_hier.get_value());
			}

		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			return String.format ("%8s%8s%8s%8s%8s%8s%8s%8s%08d",
				entity_type,
				param_data_ptr,
				structure,
				line_font,
				level,
				view,
				xform_matrix,
				label_disp,
				get_entity_status().get_value()
				);
		}

	}




	// Card for directory entry 2 record.

	public static class CardDirEntry2 extends CardDirEntry {

		// Entity type.

		protected IgesInteger entity_type;

		public IgesInteger get_entity_type () {
			return entity_type;
		}

		// Line weight number, can be 0 for the receiving system default.

		protected IgesInteger line_weight;

		public IgesInteger get_line_weight () {
			return line_weight;
		}

		// Color number (COLOR_XXXX), can be zero for no color.

		protected IgesIntegerOrPointer color;

		public IgesIntegerOrPointer get_color () {
			return color;
		}

		// Parameter data line count, must be positive except for the null entity.

		protected IgesInteger param_line_count;

		public IgesInteger get_param_line_count () {
			return param_line_count;
		}

		// Form number (entity sub-type), can be zero (default).

		protected IgesInteger form_number;

		public IgesInteger get_form_number () {
			return form_number;
		}

		// Entity label (max 8 characters, preferably uppercase).

		protected IgesLiteralString entity_label;

		public IgesLiteralString get_entity_label () {
			return entity_label;
		}

		// Entity subscript.

		protected IgesInteger entity_subscript;

		public IgesInteger get_entity_subscript () {
			return entity_subscript;
		}

		// Constructor.

		public CardDirEntry2 (IgesPointer seq_num,
				IgesInteger entity_type,
				IgesInteger line_weight,
				IgesIntegerOrPointer color,
				IgesInteger param_line_count,
				IgesInteger form_number,
				IgesLiteralString entity_label,
				IgesInteger entity_subscript) {
			super (seq_num, false);
			this.entity_type = entity_type;
			this.line_weight = line_weight;
			this.color = color;
			this.param_line_count = param_line_count;
			this.form_number = form_number;
			this.entity_label = entity_label;
			this.entity_subscript = entity_subscript;

			if (!( line_weight.get_value() >= 0 )) {
				throw new IllegalArgumentException ("CardDirEntry2: Invalid line_weight: " + line_weight.get_value());
			}

			if (!( color.get_value() <= COLOR_WHITE )) {
				throw new IllegalArgumentException ("CardDirEntry2: Invalid color: " + color.get_value());
			}

			if (!( entity_label.get_num_chars() <= 8 )) {
				throw new IllegalArgumentException ("CardDirEntry2: Invalid entity_label: " + entity_label.get_value());
			}

			if (!( entity_subscript.get_value() >= 0 && entity_subscript.get_value() <= 99999999 )) {
				throw new IllegalArgumentException ("CardDirEntry2: Invalid entity_subscript: " + entity_subscript.get_value());
			}

		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			return String.format ("%8s%8s%8s%8s%8s%8s%8s%8s%8s",
				entity_type,
				line_weight,
				color,
				param_line_count,
				form_number,
				"",
				"",
				entity_label,
				entity_subscript
				);
		}

	}




	// Card for parameter data record.

	public static class CardParamData extends CardBase {

		// Pointer to first directory entry record.

		protected IgesPointer de_backptr;

		public IgesPointer get_de_backptr () {
			return de_backptr;
		}

		// Parameters (the entity type number must be the first parameter).

		protected IgesData[] params;

		public int get_param_count () {
			return params.length;
		}

		public IgesData get_param (int i) {
			return params[i];
		}

		// Constructor.
		// Total length of parameters, including 1 char per parameter for separators, should not exceed CW_PARAM_DATA = 64 characters.

		public CardParamData (IgesPointer seq_num, boolean eor, IgesPointer de_backptr, List<IgesData> params) {
			super (LC_PARAM_DATA, seq_num, eor);
			this.de_backptr = de_backptr;
			this.params = params.toArray (new IgesData[0]);
		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			StringBuilder sb = new StringBuilder();
			int n = params.length;
			for (int i = 0; i < n; ++i) {
				sb.append (params[i].toString());
				sb.append ((eor && (i+1) == n) ? REC_TERM : PARAM_SEP);
			}
			return String.format ("%-64s %7s", sb.toString(), de_backptr);
		}

	}




	// Card for terminate record.

	public static class CardTerminate extends CardBase {

		// Pointer to last start record.

		protected IgesPointer last_start;

		public IgesPointer get_last_start () {
			return last_start;
		}

		// Pointer to last global record.

		protected IgesPointer last_global;

		public IgesPointer get_last_global () {
			return last_global;
		}

		// Pointer to last directory entry record.

		protected IgesPointer last_dir_entry;

		public IgesPointer get_last_dir_entry () {
			return last_dir_entry;
		}

		// Pointer to last parameter data record.

		protected IgesPointer last_param_data;

		public IgesPointer get_last_param_data () {
			return last_param_data;
		}

		// Constructor.

		public CardTerminate (IgesPointer seq_num, 
				IgesPointer last_start,
				IgesPointer last_global,
				IgesPointer last_dir_entry,
				IgesPointer last_param_data) {
			super (LC_TERMINATE, seq_num, true);
			this.last_start = last_start;
			this.last_global = last_global;
			this.last_dir_entry = last_dir_entry;
			this.last_param_data = last_param_data;

			if (!( last_start.get_value() > 0 )) {
				throw new IllegalArgumentException ("CardTerminate: Invalid last_start: " + last_start.get_value());
			}

			if (!( last_global.get_value() > 0 )) {
				throw new IllegalArgumentException ("CardTerminate: Invalid last_global: " + last_global.get_value());
			}

			if (!( last_dir_entry.get_value() > 0 )) {
				throw new IllegalArgumentException ("CardTerminate: Invalid last_dir_entry: " + last_dir_entry.get_value());
			}

			if (!( last_param_data.get_value() > 0 )) {
				throw new IllegalArgumentException ("CardTerminate: Invalid last_param_data: " + last_param_data.get_value());
			}

		}

		// Hook method used by subclasses to return the first 72 columns.

		protected String hook_72_cols () {
			return String.format ("%1s%7s%1s%7s%1s%7s%1s%7s%8s%8s%8s%8s%8s",
				LC_START, last_start,
				LC_GLOBAL, last_global,
				LC_DIR_ENTRY, last_dir_entry,
				LC_PARAM_DATA, last_param_data,
				"",
				"",
				"",
				"",
				""
				);
		}

	}




	//----- IGES card container -----




	// List of cards.

	public static class IgesCardList<T extends CardBase> extends ArrayList<T> {

		// Get a pointer to the last card in the list.
		// Note: Pointers are 1-based.

		public IgesPointer get_last_ptr () {
			return new IgesPointer (size());
		}

		// Get a pointer to the next card that will be added to the list.
		// Note: Pointers are 1-based.

		public IgesPointer get_next_ptr () {
			return new IgesPointer (size() + 1);
		}

		// Write the list of cards.

		public void write_to (Writer writer) throws IOException {
			for (CardBase card : this) {
				writer.write (card.toString() + LINE_TERM);
			}
			return;
		}

	}




	//----- IGES file contents -----




	// Start section.

	public IgesCardList<CardStart> start_section;

	// Global section.

	public IgesCardList<CardGlobal> global_section;

	// Directory entry section.

	public IgesCardList<CardDirEntry> dir_entry_section;

	// Parameter data section.

	public IgesCardList<CardParamData> param_data_section;

	// Terminate section.

	public IgesCardList<CardTerminate> terminate_section;




	// Construct an empty file.

	public IgesFile () {
		start_section = new IgesCardList<CardStart>();
		global_section = new IgesCardList<CardGlobal>();
		dir_entry_section = new IgesCardList<CardDirEntry>();
		param_data_section = new IgesCardList<CardParamData>();
		terminate_section = new IgesCardList<CardTerminate>();
	}




	// Clear the file.

	public void clear () {
		start_section = new IgesCardList<CardStart>();
		global_section = new IgesCardList<CardGlobal>();
		dir_entry_section = new IgesCardList<CardDirEntry>();
		param_data_section = new IgesCardList<CardParamData>();
		terminate_section = new IgesCardList<CardTerminate>();
		return;
	}




	// Add a start card.
	// Parameters:
	//  eor = True if this is the last start record.
	//  text = Text to appear in the record, can be empty if none desired.
	// Returns the sequence number of the new card.

	public IgesPointer add_start_card (boolean eor, String text) {
		IgesPointer seq = start_section.get_next_ptr();
		start_section.add (new CardStart (seq, eor, new IgesLiteralString (text)));
		return seq;
	}




	// Add the global cards.
	// Parameters:
	//  global_data = List of global parameters.
	// Returns the sequence number of the first new card.

	public IgesPointer add_global_cards (IgesDataList global_data) {
		IgesPointer first_seq = global_section.get_next_ptr();

		// Need to split up the global parameters among multiple cards

		int begin = 0;
		boolean eor = false;
		while (!eor) {
			IgesPointer seq = global_section.get_next_ptr();
			int end = global_data.find_range_in (begin, CW_GLOBAL);
			if (end == global_data.size()) {
				eor = true;
			}
			List<IgesData> params = global_data.subList (begin, end);
			global_section.add (new CardGlobal (seq, eor, params));
			begin = end;
		}
	
		return first_seq;
	}




	// Add the directory entry and parameter data cards.
	// Parameters:
	//  dir_entry_data = List of directory entry parameters, excluding the
	//                   entity type, parameter data pointer, and parameter line count.
	//  param_data = Parameter data, beginning with the entity type.
	// Returns the sequence number of the first new directory entry card.

	public IgesPointer add_param_cards (IgesDataList dir_entry_data, IgesDataList param_data) {
		IgesPointer first_de_seq = dir_entry_section.get_next_ptr();
		IgesPointer first_pd_seq = param_data_section.get_next_ptr();

		// Need to split up the parameters among multiple cards

		int pd_card_count = 0;
		int begin = 0;
		boolean eor = false;
		while (!eor) {
			++pd_card_count;
			IgesPointer pd_seq = param_data_section.get_next_ptr();
			int end = param_data.find_range_in (begin, CW_PARAM_DATA);
			if (end == param_data.size()) {
				eor = true;
			}
			List<IgesData> params = param_data.subList (begin, end);
			param_data_section.add (new CardParamData (pd_seq, eor, first_de_seq, params));
			begin = end;
		}

		// First directory entry card

		IgesPointer seq_num					= dir_entry_section.get_next_ptr();
		IgesInteger entity_type				= param_data.get_integer (0);
		IgesPointer param_data_ptr			= first_pd_seq;
		IgesIntegerOrPointer structure		= dir_entry_data.get_integer_or_pointer (0);
		IgesIntegerOrPointer line_font		= dir_entry_data.get_integer_or_pointer (1);
		IgesIntegerOrPointer level			= dir_entry_data.get_integer_or_pointer (2);
		IgesPointer view					= dir_entry_data.get_pointer (3);
		IgesPointer xform_matrix			= dir_entry_data.get_pointer (4);
		IgesPointer label_disp				= dir_entry_data.get_pointer (5);
		IgesInteger entity_status_blank		= dir_entry_data.get_integer (6);
		IgesInteger entity_status_subord	= dir_entry_data.get_integer (7);
		IgesInteger entity_status_use		= dir_entry_data.get_integer (8);
		IgesInteger entity_status_hier		= dir_entry_data.get_integer (9);

		dir_entry_section.add (new CardDirEntry1 (
			seq_num,
			entity_type,
			param_data_ptr,
			structure,
			line_font,
			level,
			view,
			xform_matrix,
			label_disp,
			entity_status_blank,
			entity_status_subord,
			entity_status_use,
			entity_status_hier));

		// Second directory entry card

		seq_num								= dir_entry_section.get_next_ptr();
		entity_type							= param_data.get_integer (0);
		IgesInteger line_weight				= dir_entry_data.get_integer (10);
		IgesIntegerOrPointer color			= dir_entry_data.get_integer_or_pointer (11);
		IgesInteger param_line_count		= new IgesInteger (pd_card_count);
		IgesInteger form_number				= dir_entry_data.get_integer (12);
		IgesLiteralString entity_label		= dir_entry_data.get_literal_string (13);
		IgesInteger entity_subscript		= dir_entry_data.get_integer (14);

		dir_entry_section.add (new  CardDirEntry2 (
			seq_num,
			entity_type,
			line_weight,
			color,
			param_line_count,
			form_number,
			entity_label,
			entity_subscript));
	
		return first_de_seq;
	}




	// Add a terminate card.
	// Parameters:
	// Returns the sequence number of the new card.

	public IgesPointer add_terminate_card () {

		IgesPointer seq_num				= terminate_section.get_next_ptr();
		IgesPointer last_start			= start_section.get_last_ptr();
		IgesPointer last_global			= global_section.get_last_ptr();
		IgesPointer last_dir_entry		= dir_entry_section.get_last_ptr();
		IgesPointer last_param_data		= param_data_section.get_last_ptr();

		terminate_section.add (new CardTerminate (
			seq_num, 
			last_start,
			last_global,
			last_dir_entry,
			last_param_data));

		return seq_num;
	}




	// Write the file, to a writer.

	public void write_to (Writer writer) throws IOException {
		start_section.write_to (writer);
		global_section.write_to (writer);
		dir_entry_section.write_to (writer);
		param_data_section.write_to (writer);
		terminate_section.write_to (writer);
		return;
	}




	// Write the file, to a filename.

	public void write_to (String filename) {
		try (
			Writer writer = new BufferedWriter (new FileWriter (filename));
		) {
			write_to (writer);
		}
		catch (IOException e) {
			throw new RuntimeException ("IgesFile: Error writing to file: " + filename, e);
		}
		return;
	}




	//----- Testing -----




	public static void main(String[] args) {

		// There needs to be at least one argument, which is the subcommand

		if (args.length < 1) {
			System.err.println ("IgesFile : Missing subcommand");
			return;
		}




		// Subcommand : Test #1
		// Command format:
		//  test1  real_value
		// Create IgesFloat and IgesDouble objects for the given real value, and display them.

		if (args[0].equalsIgnoreCase ("test1")) {

			// 1 additional argument

			if (args.length != 2) {
				System.err.println ("IgesFile : Invalid 'test1' subcommand");
				return;
			}

			double real_value = Double.parseDouble (args[1]);

			// Create the objects

			IgesFloat iges_float = new IgesFloat (real_value);
			IgesDouble iges_double = new IgesDouble (real_value);

			// Display it

			System.out.println ("IgesFloat: " + iges_float.toString());
			System.out.println ("IgesDouble: " + iges_double.toString());

			return;
		}




		// Unrecognized subcommand.

		System.err.println ("IgesFile : Unrecognized subcommand : " + args[0]);
		return;

	}




}
