#ifndef _MATCH_
#define _MATCH_

#define _USE_MATH_DEFINES
#include "Common.h"
#include "Database.h"
#include "lp_lib.h"

class CMatch{

public:
	
		lprec		*lp;
		int			Ncol, *colno = NULL, j, ret = 0;
		REAL		*row = NULL;
	
		/* We will build the model row by row
		So we start with creating a model with 0 rows and 2 columns */
		int			user_no;
		int			cloud_no;

		CMatch();
		~CMatch();

		void NormalizeFactor();
		void InsertWeightTable();
		vector <match_result_data> CalculateLP();
		double CalculateDistEp1(coord_value stUserCoordValue);
		double CalculateDistEp2(coord_value stUserCoordValue);
		double CalculateDistEp3(coord_value stUserCoordValue);
		int FindMax(vector <int> vec);
		int FindMin(vector <int> vec);
		double FindMaxDist(double *arrDists);
		double FindMinDist(double *arrDists);
		double DegToRad(double dDeg);
		double RadToDeg(double dRad);
};

#endif