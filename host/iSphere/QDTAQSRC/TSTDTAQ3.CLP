     /* --------------------------------------------------------------------- */
     /*                                                                       */
     /* Copyright (c) 2012-2016 iSphere Project Owners                        */
     /* All rights reserved. This program and the accompanying materials      */
     /* are made available under the terms of the Common Public License v1.0  */
     /* which accompanies this distribution, and is available at              */
     /* http://www.eclipse.org/legal/cpl-v10.html                             */
     /*                                                                       */
     /* --------------------------------------------------------------------- */
     /*                                                                       */
     /* iSphere Test Data Queue - Sequention LIFO Keyed                       */
     /*                                                                       */
     /* --------------------------------------------------------------------- */
     /* STRPREPRC Compile Options:                                            */
     /*   >>PRE-COMPILER<<                                                    */
     /*     >>CRTCMD<<    CRTCLPGM   PGM(QTEMP/&OB) +                         */
     /*                              SRCFILE(&SL/&SF) SRCMBR(&SM);            */
     /*       >>COMPILE<<                                                     */
     /*         >>PARM<<  TEXT('Creates: iSphere Test Data Queue - KEYED');   */
     /*         >>PARM<<  OPTION(*LSTDBG);                                    */
     /*         >>PARM<<  TGTRLS(&TR);                                        */
     /*       >>END-COMPILE<<                                                 */
     /*       >>EXECUTE<<                                                     */
     /*       >>CMD<<     CALL PGM(QTEMP/&OB) PARM(&LI &OB);                  */
     /*   >>END-PRE-COMPILER<<                                                */
     /* --------------------------------------------------------------------- */
             PGM        PARM(&LIB &OBJ)

             DCL        VAR(&LIB     ) TYPE(*CHAR) LEN(10)
             DCL        VAR(&OBJ     ) TYPE(*CHAR) LEN(10)

             DCL        VAR(&LENGTH  ) TYPE(*DEC ) LEN(5  0) VALUE(120)
             DCL        VAR(&DATA    ) TYPE(*CHAR) LEN(120 )
             DCL        VAR(&COUNT   ) TYPE(*DEC ) LEN(5  0)
             DCL        VAR(&COUNT_A ) TYPE(*CHAR) LEN(5)
             DCL        VAR(&KEY     ) TYPE(*CHAR) LEN(7)
             DCL        VAR(&KEYLEN  ) TYPE(*DEC ) LEN(3  0) VALUE(7)
             DCL        VAR(&KEYPRFX ) TYPE(*CHAR) LEN(2   ) VALUE('K:')

             /* ------------------------- Error handler ------------------------- */
             DCL        VAR(&ERRMSGKEY ) TYPE(*CHAR) LEN( 4)
             DCL        VAR(&ERRMSGTYPE) TYPE(*CHAR) LEN(10) VALUE('*DIAG')
             DCL        VAR(&ERRNUMTYPE) TYPE(*CHAR) LEN( 4) VALUE(x'00000001')
             DCL        VAR(&ERRSTKE   ) TYPE(*CHAR) LEN(10) VALUE('*PGMBDY')
             DCL        VAR(&ERRSTKCNT ) TYPE(*CHAR) LEN( 4) VALUE(x'00000001')
             DCL        VAR(&ERRCODE   ) TYPE(*CHAR) LEN( 8) +
                          VALUE(x'0000000000000000')

             MONMSG     MSGID(CPF0000) EXEC(GOTO ERRHDLR)
             /* ----------------------------------------------------------------- */

             DLTDTAQ    DTAQ(&LIB/&OBJ)
             MONMSG     MSGID(CPF2105) EXEC(DO)
                RMVMSG     PGMQ(*SAME (*)) CLEAR(*ALL)
             ENDDO

             CRTDTAQ    DTAQ(&LIB/&OBJ) TYPE(*STD) MAXLEN(&LENGTH) KEYLEN(&KEYLEN) +
                          SEQ(*KEYED) SENDERID(*YES) +
                          AUTORCL(*YES) SIZE(32 16) +
                          TEXT('iSphere Test Data Queue - Sequention KEYED')

             CHGVAR     VAR(&COUNT) VALUE(3)
             CHGVAR     VAR(&COUNT_A) VALUE(&COUNT)
             CHGVAR     VAR(&DATA) VALUE(&COUNT_A *CAT ' - Hello World')
             CHGVAR     VAR(&KEY) VALUE(&KEYPRFX *CAT &COUNT_A *CAT ' - Hello World')
             CALL       PGM(QSNDDTAQ) PARM(&OBJ &LIB &LENGTH &DATA &KEYLEN &KEY)

             CHGVAR     VAR(&COUNT) VALUE(2)
             CHGVAR     VAR(&COUNT_A) VALUE(&COUNT)
             CHGVAR     VAR(&DATA) VALUE(&COUNT_A *CAT ' - Hello World')
             CHGVAR     VAR(&KEY) VALUE(&KEYPRFX *CAT &COUNT_A *CAT ' - Hello World')
             CALL       PGM(QSNDDTAQ) PARM(&OBJ &LIB &LENGTH &DATA &KEYLEN &KEY)

             CHGVAR     VAR(&COUNT) VALUE(4)
             CHGVAR     VAR(&COUNT_A) VALUE(&COUNT)
             CHGVAR     VAR(&DATA) VALUE(&COUNT_A *CAT ' - Hello World')
             CHGVAR     VAR(&KEY) VALUE(&KEYPRFX *CAT &COUNT_A *CAT ' - Hello World')
             CALL       PGM(QSNDDTAQ) PARM(&OBJ &LIB &LENGTH &DATA &KEYLEN &KEY)

             CHGVAR     VAR(&COUNT) VALUE(1)
             CHGVAR     VAR(&COUNT_A) VALUE(&COUNT)
             CHGVAR     VAR(&DATA) VALUE(&COUNT_A *CAT ' - Hello World')
             CHGVAR     VAR(&KEY) VALUE(&KEYPRFX *CAT &COUNT_A *CAT ' - Hello World')
             CALL       PGM(QSNDDTAQ) PARM(&OBJ &LIB &LENGTH &DATA &KEYLEN &KEY)

             CHGVAR     VAR(&COUNT) VALUE(5)
             CHGVAR     VAR(&COUNT_A) VALUE(&COUNT)
             CHGVAR     VAR(&DATA) VALUE(&COUNT_A *CAT ' - Hello World')
             CHGVAR     VAR(&KEY) VALUE(&KEYPRFX *CAT &COUNT_A *CAT ' - Hello World')
             CALL       PGM(QSNDDTAQ) PARM(&OBJ &LIB &LENGTH &DATA &KEYLEN &KEY)

             RETURN

             /* --------------------------------------------------- */
             /*   Error Handler                                     */
             /* --------------------------------------------------- */
 ERRHDLR:    CALL       PGM(QMHMOVPM) PARM(&ERRMSGKEY &ERRMSGTYPE &ERRNUMTYPE +
                          &ERRSTKE &ERRSTKCNT &ERRCODE    )
             MONMSG     MSGID(CPF0000)

             CALL       PGM(QMHRSNEM) PARM(&ERRMSGKEY &ERRCODE    )

             ENDPGM
