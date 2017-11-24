#include "qrad.h"

#define NUM_LERP_POINTS 4 //used by PointInWall (defines magic number)

//===============================================
// PointInEdge - base for all boundary checks
//===============================================

inline bool PointInEdge(const vec_t* const point, const vec_t* const p1, const vec_t* const p2, const vec_t* const normal)
{
	vec3_t edge, temp, temp2; //don't make static because we need multithread support
	VectorSubtract(p1,p2,edge);
	VectorSubtract(p1,point,temp);
	CrossProduct(edge,temp,temp2);
	//old code normalizes here, which is useless when detecting sign change below
	return (DotProduct(temp2,normal) >= 0);
}

//===============================================
// PointInWall - check a lerpWall_t boundary
//===============================================

inline bool PointInWall(const lerpWall_t* const wall, const vec_t* const point)
{
	for(int counter = 0; counter < NUM_LERP_POINTS; counter++)
	{
		if(!PointInEdge(point,wall->vertex[counter],wall->vertex[(counter+1)%4],wall->plane.normal))
		{ return false; }
	}
	return true;
}

//===============================================
// PointInWall - check a Winding boundary
//===============================================

inline bool PointInWinding(const Winding* const W, const dplane_t* const plane, const vec_t* const point)
{
	//reverse direction of points because plane input is 180 degrees from desired normal
	for(size_t counter = 0; counter < W->m_NumPoints-1; counter++)
	{
		if(!PointInEdge(point,W->m_Points[counter+1],W->m_Points[counter],plane->normal))
		{ return false; }
	}
	return PointInEdge(point,W->m_Points[0],W->m_Points[W->m_NumPoints-1],plane->normal);
}

//==================================================
// PointInTri - check a boundary defined by 3 points
//==================================================

inline bool PointInTri(const vec_t* const point, const dplane_t* const plane, const vec_t* const p1, const vec_t* const p2, const vec_t* const p3)
{
	return (PointInEdge(point,p1,p2,plane->normal) && PointInEdge(point,p2,p3,plane->normal) && PointInEdge(point,p3,p1,plane->normal));
}

//==================================================
// LineSegmentIntersectsBounds - does the line pass through the box?
//==================================================

inline bool LineSegmentIntersectsBounds(const vec_t* const p1, const vec_t* const p2, const vec3_t& mins, const vec3_t& maxs)
{
	vec_t tNear = -999999999;
	vec_t tFar  =  999999999;
	vec_t t0,t1,tmp;

	vec3_t d;
	VectorSubtract(p2,p1,d);

	for(int index = 0; index < 3; ++index)
	{
		if(fabs(d[index]) < EQUAL_EPSILON)
		{
			if(p1[index] < (mins[index] - EQUAL_EPSILON) || p1[index] > (maxs[index] + EQUAL_EPSILON))
			{
				return false;
			}
			continue;
		}

		t0 = (mins[index] - p1[index]) / d[index];
		t1 = (maxs[index] - p1[index]) / d[index];

		if(t0 > t1)
		{
			tmp = t1;
			t1 = t0;
			t0 = tmp;
		}

		if(t0 > tNear)
		{
			tNear = t0;
		}

		if(t1 < tFar)
		{
			tFar = t1;
		}

		if(tNear > tFar || tFar < 0)
		{
			return false;
		}
	}

	return true;
}

//==================================================
// LineSegmentIntersectsFace - does the face block the segment?
//==================================================

inline bool LineSegmentIntersectsFace(const vec_t* const p1, const vec_t* const p2, vec3_t& point_out, const int index)
{
	if(LineSegmentIntersectsBounds(p1,p2,g_opaque_face_list[index].mins,g_opaque_face_list[index].maxs))
	{
		if(LineSegmentIntersectsPlane(g_opaque_face_list[index].plane,p1,p2,point_out))
		{
			if(PointInWinding(g_opaque_face_list[index].winding,&g_opaque_face_list[index].plane,point_out))
			{
				return true;
			}
		}
	}
	return false;
}

//==================================================
// LineSegmentIntersectsPlane - returns intersection
// point in point parameter if it exists
//==================================================

inline bool LineSegmentIntersectsPlane(const dplane_t& plane, const vec_t* const p1, const vec_t* const p2, vec3_t& point)
{
	vec3_t line;
	VectorSubtract(p2,p1,line);
	vec_t dist1 = DotProduct(plane.normal,line); //p1 to p2

	if(dist1 == 0.0) //parallel
	{ return false; }

	vec3_t origin;
	VectorScale(plane.normal,plane.dist,origin);
	VectorSubtract(origin,p1,origin);

	vec_t loc = DotProduct(plane.normal,origin)/dist1; //p1 to plane
	if(loc < 0 || loc > 1) //if we're over 100% or 
	{ return false; } //under 0% of original length

	VectorMA(p1,loc,line,point);
	return true;
}

//==================================================
// PlaneFromPoints
//==================================================

inline void PlaneFromPoints(const vec_t* const p1, const vec_t* const p2, const vec_t* const p3, dplane_t* plane)
{
	vec3_t	temp, temp2;
	VectorSubtract(p3,p2,temp);
	VectorSubtract(p1,p2,temp2);
	CrossProduct(temp,temp2,plane->normal);
	VectorNormalize(plane->normal);
	plane->dist = DotProduct(plane->normal,p1);
}

//==================================================
// SnapToPlane
//==================================================

inline void SnapToPlane(const dplane_t* const plane, vec_t* const point, const vec_t offset)
{
	vec_t scale = (plane->dist + offset);
	scale -= DotProduct(plane->normal,point);
	VectorMA(point,scale,plane->normal,point);
}
