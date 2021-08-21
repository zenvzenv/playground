grammar CommonRule;
SKIP_STRATEGY : ('skipToFirst'|'skipToLast'|'skipPastLastEvent'|'skipToNext'|'noSkip');

rule : skipStrategy? condition+ times? consecutive? timeWindow?;
skipStrategy : SKIP_STRATEGY;
