package scratch.spline;

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
import java.io.File;
import java.io.FilenameFilter;

import java.nio.file.Files;
import java.nio.file.Paths;

import scratch.util.MarshalWriter;
import scratch.util.MarshalReader;
import scratch.util.MarshalImpJsonWriter;
import scratch.util.MarshalImpJsonReader;
import scratch.util.MarshalException;

import scratch.iges.IgesFile;
import scratch.iges.IgesFile.IgesPointer;
import scratch.iges.IgesFile.IgesDataList;


/**
 * Class for holding a fitted spline.
 * Author: Michael Barall 11/17/2018.
 *
 * See documentation: https://github.com/ooreilly/splinefit/tree/master/dev
 *
 */
public class FittedSpline {

	// Polynomial orders in the u and v directions.

	public int pu;
	public int pv;

	// Knot vectors in the u and v directions.
	// Dimensions: u[mu] and v[mv]
	// Where: mu is the number of knots in the u-direction, mv is the number of knots in the v-direction.

	public double[] u;
	public double[] v;

	// Control points in reference coordinates.
	// Dimensions: px[nv][nu], py[nv][nu], and pz[nv][nu]
	// Where: nv is the number of control points in the v-direction, nv = mv - pv - 1.
	// Where: nu is the number of control points in the u-direction, nu = mu - pu - 1.

	public double[][] px;
	public double[][] py;
	public double[][] pz;

	// Control points in real-world coordinates (meters).
	// Dimensions: rx[nv][nu], ry[nv][nu], and rz[nv][nu]
	// Where: nv is the number of control points in the v-direction, nv = mv - pv - 1.
	// Where: nu is the number of control points in the u-direction, nu = mu - pu - 1.

	public double[][] rx;
	public double[][] ry;
	public double[][] rz;


	// Get the number of knots in the u and v directions.

	public int get_mu () {
		return u.length;
	}

	public int get_mv () {
		return v.length;
	}


	// Get the number of control points in the u and v directions.

	public int get_nu () {
		return u.length - pu - 1;
	}

	public int get_nv () {
		return v.length - pv - 1;
	}




	//----- Construction -----




	// Default constructor.

	public FittedSpline () {
	}




	// Check to see the data is valid.

	public void check_invariant () {
	
		// Polynomial orders must be non-negative

		if (!( pu >= 0 )) {
			throw new IllegalStateException ("FittedSpline: Invalid polynomial order, pu = " + pu);
		}

		if (!( pv >= 0 )) {
			throw new IllegalStateException ("FittedSpline: Invalid polynomial order, pv = " + pv);
		}

		// Knot vectors must be in increasing order

		if (!( u != null )) {
			throw new IllegalStateException ("FittedSpline: Null knot vector u");
		}
		int mu = u.length;
		for (int j = 1; j < mu; ++j) {
			if (!( u[j-1] <= u[j] )) {
				throw new IllegalStateException ("FittedSpline: Out-of-order knot value u[j], j = " + j);
			}
		}

		if (!( v != null )) {
			throw new IllegalStateException ("FittedSpline: Null knot vector v");
		}
		int mv = v.length;
		for (int i = 1; i < mv; ++i) {
			if (!( v[i-1] <= v[i] )) {
				throw new IllegalStateException ("FittedSpline: Out-of-order knot value v[i], i = " + i);
			}
		}

		// Check control point counts

		int nu = mu - pu - 1;
		int nv = mv - pv - 1;

		if (!( nu >= 1 )) {
			throw new IllegalStateException ("FittedSpline: Invalid number of control points, nu = " + nu);
		}

		if (!( nv >= 1 )) {
			throw new IllegalStateException ("FittedSpline: Invalid number of control points, nv = " + nv);
		}

		// Check control point arrays

		check_invariant_cp (px, "px", nu, nv);
		check_invariant_cp (px, "py", nu, nv);
		check_invariant_cp (px, "pz", nu, nv);

		check_invariant_cp (px, "rx", nu, nv);
		check_invariant_cp (px, "ry", nu, nv);
		check_invariant_cp (px, "rz", nu, nv);

		return;
	}




	// Check invariant for a control point.

	private void check_invariant_cp (double[][] p, String name, int nu, int nv) {
		if (!( p != null )) {
			throw new IllegalStateException ("FittedSpline: Null control point array " + name);
		}
		if (!( p.length == nv )) {
			throw new IllegalStateException ("FittedSpline: Incorrect length for control point array " + name + ", expected " + nv + ", got " + p.length);
		}
		for (int i = 0; i < nv; ++i) {
			if (!( p[i] != null )) {
				throw new IllegalStateException ("FittedSpline: Null control point array " + name + "[" + i + "]");
			}
			if (!( p[i].length == nu )) {
				throw new IllegalStateException ("FittedSpline: Incorrect length for control point array " + name + "[" + i + "]" + ", expected " + nu + ", got " + p[i].length);
			}
		}
		return;
	}




	// Write this as a string.

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append ("FittedSpline:" + "\n");
		sb.append ("pu = " + pu + "\n");
		sb.append ("pv = " + pv + "\n");
		sb.append ("u = " + Arrays.toString (u) + "\n");
		sb.append ("v = " + Arrays.toString (v) + "\n");

		if (px == null) {
			sb.append ("px = " + "null" + "\n");
		} else {
			for (int i = 0; i < px.length; ++i) {
				sb.append ("px[" + i + "] = " + Arrays.toString (px[i]) + "\n");
			}
		}

		if (py == null) {
			sb.append ("py = " + "null" + "\n");
		} else {
			for (int i = 0; i < py.length; ++i) {
				sb.append ("py[" + i + "] = " + Arrays.toString (py[i]) + "\n");
			}
		}

		if (pz == null) {
			sb.append ("pz = " + "null" + "\n");
		} else {
			for (int i = 0; i < pz.length; ++i) {
				sb.append ("pz[" + i + "] = " + Arrays.toString (pz[i]) + "\n");
			}
		}

		if (rx == null) {
			sb.append ("rx = " + "null" + "\n");
		} else {
			for (int i = 0; i < rx.length; ++i) {
				sb.append ("rx[" + i + "] = " + Arrays.toString (rx[i]) + "\n");
			}
		}

		if (ry == null) {
			sb.append ("ry = " + "null" + "\n");
		} else {
			for (int i = 0; i < ry.length; ++i) {
				sb.append ("ry[" + i + "] = " + Arrays.toString (ry[i]) + "\n");
			}
		}

		if (rz == null) {
			sb.append ("rz = " + "null" + "\n");
		} else {
			for (int i = 0; i < rz.length; ++i) {
				sb.append ("rz[" + i + "] = " + Arrays.toString (rz[i]) + "\n");
			}
		}

		if (u != null && v != null && px != null && py != null && pz != null && rx != null && ry != null && rz != null) {
			sb.append ("mu = " + get_mu() + "\n");
			sb.append ("mv = " + get_mv() + "\n");
			sb.append ("nu = " + get_nu() + "\n");
			sb.append ("nv = " + get_nv() + "\n");

			double[] ref_bbox_lo = new double[3];
			double[] ref_bbox_hi = new double[3];
			double[] ref_max_span = new double[2];
			get_ref_bbox (ref_bbox_lo, ref_bbox_hi);
			get_span_from_bbox (ref_bbox_lo, ref_bbox_hi, ref_max_span);
			sb.append ("ref_bbox_lo = " + Arrays.toString (ref_bbox_lo) + "\n");
			sb.append ("ref_bbox_hi = " + Arrays.toString (ref_bbox_hi) + "\n");
			sb.append ("ref_max_span = " + Arrays.toString (ref_max_span) + "\n");

			double[] rw_bbox_lo = new double[3];
			double[] rw_bbox_hi = new double[3];
			double[] rw_max_span = new double[2];
			get_rw_bbox (rw_bbox_lo, rw_bbox_hi);
			get_span_from_bbox (rw_bbox_lo, rw_bbox_hi, rw_max_span);
			sb.append ("rw_bbox_lo = " + Arrays.toString (rw_bbox_lo) + "\n");
			sb.append ("rw_bbox_hi = " + Arrays.toString (rw_bbox_hi) + "\n");
			sb.append ("rw_max_span = " + Arrays.toString (rw_max_span) + "\n");
		}
	
		return sb.toString();
	}




	//----- Service functions -----




	// Get the bounding box in reference coordinates.
	// Parameters:
	//  bbox_lo = 3-element array, receives lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, receives upper limit of control point x,y,z coordinates.

	public void get_ref_bbox (double[] bbox_lo, double[] bbox_hi) {

		// Initialize bounding box, then merge

		init_bbox (bbox_lo, bbox_hi);
		merge_ref_bbox (bbox_lo, bbox_hi);

		return;
	}




	// Merge the bounding box in reference coordinates.
	// Parameters:
	//  bbox_lo = 3-element array, supplies and receives lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, supplies and receives upper limit of control point x,y,z coordinates.

	public void merge_ref_bbox (double[] bbox_lo, double[] bbox_hi) {

		// Array dimensions

		int nu = get_nu();
		int nv = get_nv();

		// Find bounds

		for (int i = 0; i < nv; ++i) {
			for (int j = 0; j < nu; ++j) {
				bbox_lo[0] = Math.min (bbox_lo[0], px[i][j]);
				bbox_lo[1] = Math.min (bbox_lo[1], py[i][j]);
				bbox_lo[2] = Math.min (bbox_lo[2], pz[i][j]);

				bbox_hi[0] = Math.max (bbox_hi[0], px[i][j]);
				bbox_hi[1] = Math.max (bbox_hi[1], py[i][j]);
				bbox_hi[2] = Math.max (bbox_hi[2], pz[i][j]);
			}
		}

		return;
	}




	// Get the bounding box in real-world coordinates.
	// Parameters:
	//  bbox_lo = 3-element array, receives lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, receives upper limit of control point x,y,z coordinates.

	public void get_rw_bbox (double[] bbox_lo, double[] bbox_hi) {

		// Initialize bounding box, then merge

		init_bbox (bbox_lo, bbox_hi);
		merge_rw_bbox (bbox_lo, bbox_hi);

		return;
	}




	// Merge the bounding box in real-world coordinates.
	// Parameters:
	//  bbox_lo = 3-element array, supplies and receives lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, supplies and receives upper limit of control point x,y,z coordinates.

	public void merge_rw_bbox (double[] bbox_lo, double[] bbox_hi) {

		// Array dimensions

		int nu = get_nu();
		int nv = get_nv();

		// Find bounds

		for (int i = 0; i < nv; ++i) {
			for (int j = 0; j < nu; ++j) {
				bbox_lo[0] = Math.min (bbox_lo[0], rx[i][j]);
				bbox_lo[1] = Math.min (bbox_lo[1], ry[i][j]);
				bbox_lo[2] = Math.min (bbox_lo[2], rz[i][j]);

				bbox_hi[0] = Math.max (bbox_hi[0], rx[i][j]);
				bbox_hi[1] = Math.max (bbox_hi[1], ry[i][j]);
				bbox_hi[2] = Math.max (bbox_hi[2], rz[i][j]);
			}
		}

		return;
	}




	// Initialize a bounding box.
	// Parameters:
	//  bbox_lo = 3-element array, receives lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, receives upper limit of control point x,y,z coordinates.

	public static void init_bbox (double[] bbox_lo, double[] bbox_hi) {

		// Initialize to extreme values

		bbox_lo[0] = Double.MAX_VALUE;
		bbox_lo[1] = Double.MAX_VALUE;
		bbox_lo[2] = Double.MAX_VALUE;

		bbox_hi[0] = - Double.MAX_VALUE;
		bbox_hi[1] = - Double.MAX_VALUE;
		bbox_hi[2] = - Double.MAX_VALUE;

		return;
	}




	// Given a bounding box, compute the span.
	// Parameters:
	//  bbox_lo = 3-element array, containing lower limit of control point x,y,z coordinates.
	//  bbox_hi = 3-element array, containing upper limit of control point x,y,z coordinates.
	//  max_span = 2-element array, recieves maximum absolute coordinate value (max_span[0]) and maximum coordinate span (max_span[1]).

	public static void get_span_from_bbox (double[] bbox_lo, double[] bbox_hi, double[] max_span) {

		// Find span

		max_span[0] = 0.0;
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_lo[0]));
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_lo[1]));
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_lo[2]));
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_hi[0]));
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_hi[1]));
		max_span[0] = Math.max (max_span[0], Math.abs (bbox_hi[2]));

		max_span[1] = 0.0;
		max_span[1] = Math.max (max_span[1], Math.abs (bbox_hi[0] - bbox_lo[0]));
		max_span[1] = Math.max (max_span[1], Math.abs (bbox_hi[1] - bbox_lo[1]));
		max_span[1] = Math.max (max_span[1], Math.abs (bbox_hi[2] - bbox_lo[2]));

		return;
	}




	//----- File access -----




	// Write the contents.

	public void write_contents (MarshalWriter writer) {

		// Check that we're valid

		check_invariant();

		// Begin the JSON object

		writer.marshalMapBegin (null);

		// Write the contents

		writer.marshalDouble2DArray ("Px", px);
		writer.marshalDouble2DArray ("Py", py);
		writer.marshalDouble2DArray ("Pz", pz);

		writer.marshalDouble2DArray ("real_world_Px", rx);
		writer.marshalDouble2DArray ("real_world_Py", ry);
		writer.marshalDouble2DArray ("real_world_Pz", rz);

		writer.marshalDoubleArray ("U", u);
		writer.marshalDoubleArray ("V", v);

		writer.marshalInt ("pu", pu);
		writer.marshalInt ("pv", pv);

		// End the JSON object

		writer.marshalMapEnd ();

		return;
	}




	// Read the contents.

	public void read_contents (MarshalReader reader) {

		// Begin the JSON object

		reader.unmarshalMapBegin (null);

		// Write the contents

		px = reader.unmarshalDouble2DArray ("Px");
		py = reader.unmarshalDouble2DArray ("Py");
		pz = reader.unmarshalDouble2DArray ("Pz");

		rx = reader.unmarshalDouble2DArray ("real_world_Px");
		ry = reader.unmarshalDouble2DArray ("real_world_Py");
		rz = reader.unmarshalDouble2DArray ("real_world_Pz");

		u = reader.unmarshalDoubleArray ("U");
		v = reader.unmarshalDoubleArray ("V");

		pu = reader.unmarshalInt ("pu");
		pv = reader.unmarshalInt ("pv");

		// End the JSON object

		reader.unmarshalMapEnd ();

		// Check that we're valid

		check_invariant();

		return;
	}




	// Convert to a JSON string.

	public String to_json_string () {

		// Make a JSON writer

		MarshalImpJsonWriter writer = new MarshalImpJsonWriter();

		// Write contents

		write_contents (writer);

		if (!( writer.check_write_complete() )) {
			throw new MarshalException ("Writer reports writing not complete");
		}

		// Get the string

		String json_string = writer.get_json_string();
		return json_string;
	}




	// Write to a file.

	public void write_to_file (Writer dest) {

		// Make a JSON writer

		MarshalImpJsonWriter writer = new MarshalImpJsonWriter();

		// Write contents

		write_contents (writer);

		if (!( writer.check_write_complete() )) {
			throw new MarshalException ("Writer reports writing not complete");
		}

		// Write the file

		writer.write_json_file (dest);
		return;
	}


	public void write_to_file (String filename) {
		try (
			Writer dest = new BufferedWriter (new FileWriter (filename));
		){
			write_to_file (dest);
		}
		catch (IOException e) {
			throw new MarshalException ("FittedSpline: Unable to write file: " + filename);
		}
		return;
	}




	// Convert from a JSON string.

	public FittedSpline from_json_string (String json_string) {
	
		// Make a JSON reader

		MarshalImpJsonReader reader = new MarshalImpJsonReader (json_string);

		// Read contents

		read_contents (reader);

		if (!( reader.check_read_complete() )) {
			throw new MarshalException ("Reader reports reading not complete");
		}

		return this;
	}




	// Read from a file.

	public FittedSpline read_from_file (Reader src) {
	
		// Make a JSON reader

		MarshalImpJsonReader reader = new MarshalImpJsonReader (src);

		// Read contents

		read_contents (reader);

		if (!( reader.check_read_complete() )) {
			throw new MarshalException ("Reader reports reading not complete");
		}

		return this;
	}


	public FittedSpline read_from_file (String filename) {
		try (
			Reader src = new BufferedReader (new FileReader (filename));
		){
			read_from_file (src);
		}
		catch (IOException e) {
			throw new MarshalException ("FittedSpline: Unable to read file: " + filename);
		}
		return this;
	}




	//----- Conversion functions -----




	// Convert one or more splines to an IGES file.
	// Parameters:
	//  iges_filename = File to write.
	//  product_id = The product ID to include inside the IGES file.
	//               Maximum length is 68 characters.  It must be non-empty.
	//  file_name = The file name to include inside the IGES file.
	//              Maximum length is 68 characters.  It must be non-empty.
	//  label_prefix = Prefix used to construct labels.  Alphanumeric characters only.
	//                 If this is "part" then successive surfaces are labeled part0, part1, etc.
	//                 Labels can be a maximum of 8 characters, therefore label_prefix can be a
	//                 maximum of 7 characters assuming no more than ten surfaces, or a
	//                 maximum of 6 characters assuming no more tha 100 surfaces, etc.
	//  fspl = Array of splines.

	public static void spline_to_iges (
		String iges_filename,
		String product_id,
		String file_name,
		String label_prefix,
		FittedSpline... fspl) {
				
		System.out.println ("Creating IGES file: " + iges_filename);

		// Calculate bounding box for the set of splines

		double[] bbox_lo = new double[3];
		double[] bbox_hi = new double[3];
		double[] max_span = new double[2];
		FittedSpline.init_bbox (bbox_lo, bbox_hi);

		for (int i = 0; i < fspl.length; ++i) {
			fspl[i].merge_rw_bbox (bbox_lo, bbox_hi);
		}

		FittedSpline.get_span_from_bbox (bbox_lo, bbox_hi, max_span);

		// Make the IGES File

		IgesFile iges_file = new IgesFile ();

		// Start record contains just the internal filename

		iges_file.add_start_card (true, file_name);

		// Global data

		IgesDataList global_data = new IgesDataList();

		//String product_id = product_id;
		//String file_name = file_name;
		int units_flag = IgesFile.UFLAG_METER;
		int line_thickness_count = 1;
		double max_line_thickness = 0.003 * max_span[1];
		double min_resolution = 0.00001 * max_span[1];
		double max_coordinate = 0.0;

		global_data.set_global_params (
			product_id,
			file_name,
			units_flag,
			line_thickness_count,
			max_line_thickness,
			min_resolution,
			max_coordinate);

		iges_file.add_global_cards (global_data);

		// Loop over splines ...

		for (int i = 0; i < fspl.length; ++i) {

			// Directory entry parameters

			IgesDataList dir_entry_data = new IgesDataList();

			Object structure = null;
			Object line_pattern = 0;
			Object level = 0;
			Object view = 0;
			Object xform_matrix = 0;
			Object label_assoc = 0;
			int status_blank = IgesFile.ESTAT_BLANK_VISIBLE;
			int status_subord = IgesFile.ESTAT_SUBORD_INDEPENDENT;
			int status_use = IgesFile.ESTAT_USE_GEOMETRY;
			int status_hier = 0;
			int line_thickness = 0;
			Object color = IgesFile.COLOR_NONE;
			int form = IgesFile.EFORM_BSS_DATA;
			String label = label_prefix + i;
			int subscript = 0;

			dir_entry_data.set_dir_entry_params (
				structure,
				line_pattern,
				level,
				view,
				xform_matrix,
				label_assoc,
				status_blank,
				status_subord,
				status_use,
				status_hier,
				line_thickness,
				color,
				form,
				label,
				subscript);

			// Parameter data

			IgesDataList param_data = new IgesDataList();

			int bss_k1 = fspl[i].get_nv() - 1;
			int bss_k2 = fspl[i].get_nu() - 1;
			int bss_m1 = fspl[i].pv;
			int bss_m2 = fspl[i].pu;
			boolean bss_closed1 = false;
			boolean bss_closed2 = false;
			boolean bss_periodic1 = false;
			boolean bss_periodic2 = false;
			double[] bss_s = fspl[i].v;
			double[] bss_t = fspl[i].u;
			double[][] bss_w = null;
			double[][] bss_x = fspl[i].rx;
			double[][] bss_y = fspl[i].ry;
			double[][] bss_z = fspl[i].rz;
			double bss_u0 = bss_s[bss_m1];
			double bss_u1 = bss_s[bss_k1 + 1];
			double bss_v0 = bss_t[bss_m2];
			double bss_v1 = bss_t[bss_k2 + 1];

			param_data.set_bss_params (
				bss_k1,
				bss_k2,
				bss_m1,
				bss_m2,
				bss_closed1,
				bss_closed2,
				bss_periodic1,
				bss_periodic2,
				bss_s,
				bss_t,
				bss_w,
				bss_x,
				bss_y,
				bss_z,
				bss_u0,
				bss_u1,
				bss_v0,
				bss_v1);

			// Add the cards

			iges_file.add_param_cards (dir_entry_data, param_data);
		}

		// Termination record

		iges_file.add_terminate_card ();

		// Write the file

		iges_file.write_to (iges_filename);

		return;
	}




	// Convert one or more spline files to an IGES file.
	// Parameters:
	//  iges_filename = File to write.
	//  product_id = The product ID to include inside the IGES file.
	//               Maximum length is 68 characters.  It must be non-empty.
	//  file_name = The file name to include inside the IGES file.
	//              Maximum length is 68 characters.  It must be non-empty.
	//  label_prefix = Prefix used to construct labels.  Alphanumeric characters only.
	//                 If this is "part" then successive surfaces are labeled part0, part1, etc.
	//                 Labels can be a maximum of 8 characters, therefore label_prefix can be a
	//                 maximum of 7 characters assuming no more than ten surfaces, or a
	//                 maximum of 6 characters assuming no more tha 100 surfaces, etc.
	//  spline_filename = Array of spline filenames.

	public static void spline_file_to_iges (
		String iges_filename,
		String product_id,
		String file_name,
		String label_prefix,
		String... spline_filename) {

		// Read the files

		FittedSpline[] fspl = new FittedSpline[spline_filename.length];

		for (int i = 0; i < spline_filename.length; ++i) {
			System.out.println ("Reading spline file: " + spline_filename[i]);
			fspl[i] = new FittedSpline();
			fspl[i].read_from_file (spline_filename[i]);
		}

		// Construct the IGES file

		spline_to_iges (
			iges_filename,
			product_id,
			file_name,
			label_prefix,
			fspl);

		return;
	}




	// Convert one or more spline files to an IGES file.
	// Parameters:
	//  iges_filename = File to write.
	//  product_id = The product ID to include inside the IGES file.
	//               Maximum length is 68 characters.  It must be non-empty.
	//  file_name = The file name to include inside the IGES file.
	//              Maximum length is 68 characters.  It must be non-empty.
	//  label_prefix = Prefix used to construct labels.  Alphanumeric characters only.
	//                 If this is "part" then successive surfaces are labeled part0, part1, etc.
	//                 Labels can be a maximum of 8 characters, therefore label_prefix can be a
	//                 maximum of 7 characters assuming no more than ten surfaces, or a
	//                 maximum of 6 characters assuming no more tha 100 surfaces, etc.
	//  dir = Directory in which to look for files.
	//  filename_pattern = Pattern used for constructing filenames.  It must be in a form
	//                     suitable as the first argument to String.format, expecting one
	//                     data value of type integer, which is the file index.
	//  first_index = First index value to use for finding files.  Successive files are
	//                found by incrementing the index.

	public static void spline_part_to_iges (
		String iges_filename,
		String product_id,
		String file_name,
		String label_prefix,
		String dir,
		String filename_pattern,
		int first_index) {

		// List of filenames found

		ArrayList<String> filenames = new ArrayList<String>();

		// Loop to search for files

		for (int index = first_index; ; ++index) {
		
			// Construct the potential filename

			File potential = new File (dir, String.format (filename_pattern, index));

			// Stop if no File

			if (!( potential.canRead() )) {
				break;
			}

			// Add file to list

			filenames.add (potential.getPath());
		}

		// Error if no files found

		if (filenames.isEmpty()) {
			System.out.println ("No files found, directory = " + dir + ", pattern = " + filename_pattern);
			return;
		}

		// Get array of filenames

		String[] spline_filename = filenames.toArray (new String[0]);

		// Construct the IGES file

		spline_file_to_iges (
			iges_filename,
			product_id,
			file_name,
			label_prefix,
			spline_filename);

		return;
	}




	//----- Testing and Commands -----




	public static void main(String[] args) {

		// There needs to be at least one argument, which is the subcommand

		if (args.length < 1) {
			System.err.println ("FittedSpline : Missing subcommand");
			return;
		}




		// Subcommand : Test #1
		// Command format:
		//  test1  in_filename
		// Read the file, and display the results.

		if (args[0].equalsIgnoreCase ("test1")) {

			// 1 additional argument

			if (args.length != 2) {
				System.err.println ("FittedSpline : Invalid 'test1' subcommand");
				return;
			}

			String in_filename = args[1];

			// Read the file

			FittedSpline fspl = new FittedSpline();
			fspl.read_from_file (in_filename);

			// Display it

			System.out.println (fspl.toString());

			return;
		}




		// Subcommand : Test #2
		// Command format:
		//  test2  in_filename  out_filename
		// Read the file, and display the results.
		// Then write to the output file.
		// Then read it back, and display the results.

		if (args[0].equalsIgnoreCase ("test2")) {

			// 2 additional arguments

			if (args.length != 3) {
				System.err.println ("FittedSpline : Invalid 'test2' subcommand");
				return;
			}

			String in_filename = args[1];
			String out_filename = args[2];

			// Read the file

			FittedSpline fspl = new FittedSpline();
			fspl.read_from_file (in_filename);

			// Display it

			System.out.println (fspl.toString());

			// Write it

			fspl.write_to_file (out_filename);

			// Read the file we just wrote

			FittedSpline fspl2 = new FittedSpline();
			fspl2.read_from_file (out_filename);

			// Display it

			System.out.println (fspl2.toString());

			return;
		}




		// Subcommand : Test #3
		// Command format:
		//  test3  iges_filename  product_id  file_name  label_prefix  spline_filename...
		// Read the spline files, and write them to an IGES file, using real-world coordinates.

		if (args[0].equalsIgnoreCase ("test3")) {

			// 5 or more additional arguments

			if (args.length < 6) {
				System.err.println ("FittedSpline : Invalid 'test3' subcommand");
				return;
			}

			String iges_filename = args[1];
			String product_id = args[2];
			String file_name = args[3];
			String label_prefix = args[4];
			String[] spline_filename = Arrays.copyOfRange (args, 5, args.length);

			// Read the files

			double[] bbox_lo = new double[3];
			double[] bbox_hi = new double[3];
			double[] max_span = new double[2];
			FittedSpline.init_bbox (bbox_lo, bbox_hi);

			FittedSpline[] fspl = new FittedSpline[spline_filename.length];

			for (int i = 0; i < spline_filename.length; ++i) {
				System.out.println ("Reading spline file: " + spline_filename[i]);
				fspl[i] = new FittedSpline();
				fspl[i].read_from_file (spline_filename[i]);
				fspl[i].merge_rw_bbox (bbox_lo, bbox_hi);
			}

			FittedSpline.get_span_from_bbox (bbox_lo, bbox_hi, max_span);

			// Make the IGES File

			IgesFile iges_file = new IgesFile ();

			// Start record contains just the internal filename

			iges_file.add_start_card (true, file_name);

			// Global data

			IgesDataList global_data = new IgesDataList();

			//String product_id = product_id;
			//String file_name = file_name;
			int units_flag = IgesFile.UFLAG_METER;
			int line_thickness_count = 1;
			double max_line_thickness = 0.003 * max_span[1];
			double min_resolution = 0.00001 * max_span[1];
			double max_coordinate = 0.0;

			global_data.set_global_params (
				product_id,
				file_name,
				units_flag,
				line_thickness_count,
				max_line_thickness,
				min_resolution,
				max_coordinate);

			iges_file.add_global_cards (global_data);

			// Loop over splines ...

			for (int i = 0; i < fspl.length; ++i) {

				System.out.println ("Converting spline surface: " + spline_filename[i]);

				// Directory entry parameters

				IgesDataList dir_entry_data = new IgesDataList();

				Object structure = null;
				Object line_pattern = 0;
				Object level = 0;
				Object view = 0;
				Object xform_matrix = 0;
				Object label_assoc = 0;
				int status_blank = IgesFile.ESTAT_BLANK_VISIBLE;
				int status_subord = IgesFile.ESTAT_SUBORD_INDEPENDENT;
				int status_use = IgesFile.ESTAT_USE_GEOMETRY;
				int status_hier = 0;
				int line_thickness = 0;
				Object color = IgesFile.COLOR_NONE;
				int form = IgesFile.EFORM_BSS_DATA;
				String label = label_prefix + i;
				int subscript = 0;

				dir_entry_data.set_dir_entry_params (
					structure,
					line_pattern,
					level,
					view,
					xform_matrix,
					label_assoc,
					status_blank,
					status_subord,
					status_use,
					status_hier,
					line_thickness,
					color,
					form,
					label,
					subscript);

				// Parameter data

				IgesDataList param_data = new IgesDataList();

				int bss_k1 = fspl[i].get_nv() - 1;
				int bss_k2 = fspl[i].get_nu() - 1;
				int bss_m1 = fspl[i].pv;
				int bss_m2 = fspl[i].pu;
				boolean bss_closed1 = false;
				boolean bss_closed2 = false;
				boolean bss_periodic1 = false;
				boolean bss_periodic2 = false;
				double[] bss_s = fspl[i].v;
				double[] bss_t = fspl[i].u;
				double[][] bss_w = null;
				double[][] bss_x = fspl[i].rx;
				double[][] bss_y = fspl[i].ry;
				double[][] bss_z = fspl[i].rz;
				double bss_u0 = bss_s[bss_m1];
				double bss_u1 = bss_s[bss_k1 + 1];
				double bss_v0 = bss_t[bss_m2];
				double bss_v1 = bss_t[bss_k2 + 1];

				param_data.set_bss_params (
					bss_k1,
					bss_k2,
					bss_m1,
					bss_m2,
					bss_closed1,
					bss_closed2,
					bss_periodic1,
					bss_periodic2,
					bss_s,
					bss_t,
					bss_w,
					bss_x,
					bss_y,
					bss_z,
					bss_u0,
					bss_u1,
					bss_v0,
					bss_v1);

				// Add the cards

				iges_file.add_param_cards (dir_entry_data, param_data);
			}

			// Termination record

			iges_file.add_terminate_card ();

			// Write the file
				
			System.out.println ("Writing IGES file: " + iges_filename);

			iges_file.write_to (iges_filename);

			return;
		}




		// Command : file_to_iges
		// Command format:
		//  file_to_iges  iges_filename  product_id  file_name  label_prefix  spline_filename...
		// Read the spline files, and write them to an IGES file, using real-world coordinates.

		if (args[0].equalsIgnoreCase ("file_to_iges")) {

			// 5 or more additional arguments

			if (args.length < 6) {
				System.err.println ("FittedSpline : Invalid 'file_to_iges' command");
				return;
			}

			String iges_filename = args[1];
			String product_id = args[2];
			String file_name = args[3];
			String label_prefix = args[4];
			String[] spline_filename = Arrays.copyOfRange (args, 5, args.length);

			// Perform the conversion

			FittedSpline.spline_file_to_iges (
				iges_filename,
				product_id,
				file_name,
				label_prefix,
				spline_filename);

			return;
		}




		// Command : part_to_iges
		// Command format:
		//  part_to_iges  iges_filename  product_id  file_name  label_prefix  dir  filename_pattern  first_index
		// Read the spline part files, and write them to an IGES file, using real-world coordinates.

		if (args[0].equalsIgnoreCase ("part_to_iges")) {

			// 7 additional arguments

			if (args.length != 8) {
				System.err.println ("FittedSpline : Invalid 'part_to_iges' command");
				return;
			}

			String iges_filename = args[1];
			String product_id = args[2];
			String file_name = args[3];
			String label_prefix = args[4];
			String dir = args[5];
			String filename_pattern = args[6];
			int first_index = Integer.parseInt (args[7]);

			// Perform the conversion

			FittedSpline.spline_part_to_iges (
				iges_filename,
				product_id,
				file_name,
				label_prefix,
				dir,
				filename_pattern,
				first_index);

			return;
		}




		// Command : cfm_pilot_to_iges
		// Command format:
		//  cfm_pilot_to_iges  src_dir  dest_dir
		// Convert CFM pilot files to IGES files, in a parallel directory structure.

		if (args[0].equalsIgnoreCase ("cfm_pilot_to_iges")) {

			// 2 additional arguments

			if (args.length != 3) {
				System.err.println ("FittedSpline : Invalid 'cfm_pilot_to_iges' command");
				return;
			}

			String src_dir = args[1];
			String dest_dir = args[2];

			// List of CFM test directories

			String[] cfm_test_dirs = {
				"GRFS-GRFZ-WEST-Garlock_fault-CFM5",
				"PNRA-CEPS-LABS-Compton-Los_Alamitos_fault-CFM2",
				"PNRA-CRSF-USAV-Fontana_Seismicity_lineament-CFM1",
				"PNRA-CSTL-SJQH-San_Joaquin_Hills_fault-truncated-CFM3",
				"PNRA-NIRC-LABS-Newport-Inglewood_fault-dip_w_splays-split-CFM5",
				"SAFS-SAFZ-COAV-Southern_San_Andreas_fault-CFM4",
				"SAFS-SAFZ-MULT-Garnet_Hill_fault_strand-CFM4",
				"WTRA-NCVS-VNTB-Southern_San_Cayetano_fault-steep-JHAP-CFM5",
				"WTRA-ORFZ-SFNV-Northridge-Frew_fault-CFM2",
				"WTRA-SBTS-SMMT-Santa_Monica_thrust_fault-CFM1",
				"WTRA-SFFS-SMMT-Santa_Monica_fault-steep-CFM5",
				"WTRA-SSFZ-MULT-Santa_Susana_fault-CFM1"
			};

			// Loop over test directories ...

			for (String test_dir : cfm_test_dirs) {

				// Make source and destination directories

				String sdir = (new File (src_dir, test_dir)).getPath();
				String ddir = (new File (dest_dir, test_dir)).getPath();

				// Create the destination directory if necessary

				try {
					Files.createDirectories (Paths.get (ddir));
				}
				catch (IOException e) {
					throw new RuntimeException ("Error creating directory: " + ddir, e);
				}

				// Perform the conversion

				String iges_filename = (new File (ddir, "bspline_surf_fit.iges")).getPath();
				String product_id = "bspline_surf_fit";
				String file_name = "bspline_surf_fit.iges";
				String label_prefix = "part";
				String dir = sdir;
				String filename_pattern = "part_%d_bspline_surf_fit.json";
				int first_index = 0;

				FittedSpline.spline_part_to_iges (
					iges_filename,
					product_id,
					file_name,
					label_prefix,
					dir,
					filename_pattern,
					first_index);
			}

			return;
		}




		// Command : cfm_pilot_to_iges_2
		// Command format:
		//  cfm_pilot_to_iges_2  src_dir  dest_dir
		// Convert CFM pilot files to IGES files, in a parallel directory structure.
		// This is the same as cfm_pilot_to_iges, except that the directory names
		// are read from disk instead of being given in a pre-programmed list.

		if (args[0].equalsIgnoreCase ("cfm_pilot_to_iges_2")) {

			// 2 additional arguments

			if (args.length != 3) {
				System.err.println ("FittedSpline : Invalid 'cfm_pilot_to_iges_2' command");
				return;
			}

			String src_dir = args[1];
			String dest_dir = args[2];

			// List of CFM test directories, it is the first-level subdirectoris of the source directory

			File src_dir_file = new File (src_dir);
			String[] cfm_test_dirs = src_dir_file.list (new FilenameFilter() {
				@Override
				public boolean accept (File dir, String name) {
					return (new File (dir, name)).isDirectory();
				}
			});

			// Loop over test directories ...

			for (String test_dir : cfm_test_dirs) {

				// Make source and destination directories

				String sdir = (new File (src_dir, test_dir)).getPath();
				String ddir = (new File (dest_dir, test_dir)).getPath();

				// Create the destination directory if necessary

				try {
					Files.createDirectories (Paths.get (ddir));
				}
				catch (IOException e) {
					throw new RuntimeException ("Error creating directory: " + ddir, e);
				}

				// Perform the conversion

				String iges_filename = (new File (ddir, "bspline_surf_fit.iges")).getPath();
				String product_id = "bspline_surf_fit";
				String file_name = "bspline_surf_fit.iges";
				String label_prefix = "part";
				String dir = sdir;
				String filename_pattern = "part_%d_bspline_surf_fit.json";
				int first_index = 0;

				FittedSpline.spline_part_to_iges (
					iges_filename,
					product_id,
					file_name,
					label_prefix,
					dir,
					filename_pattern,
					first_index);
			}

			return;
		}




		// Unrecognized subcommand.

		System.err.println ("FittedSpline : Unrecognized subcommand : " + args[0]);
		return;

	}




}
