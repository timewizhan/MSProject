#ifndef _MATCH_
#define _MATCH_

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
		void CalculateLP();

		int FindMax(vector <int> vec);
		int FindMin(vector <int> vec);
};

#endif