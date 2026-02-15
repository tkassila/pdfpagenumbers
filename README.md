# pdfpagenumbers

This is javafx gui and commandline application, which executing with java jre 11 or later. It is adding page number or another commandline option text or like into lower page headers. 

## pdfpagenumbers.sh --help:

Usage: PdfPageNumbersCmdline input.pdf output.pdf
Usage: PdfPageNumbersCmdline input.pdf output.pdf -command="pagenumbers_left|pagenumbers_center|pagenumbers_right"
Usage: PdfPageNumbersCmdline input.pdf output.pdf -command="printtime_left|printtime_center|printtime_right [printtime_text='ddd ddd dddd']"
Usage: PdfPageNumbersCmdline input.pdf output.pdf -command="printtext_left='ddfd'|printtext_center='eee'|printtext_right='eee' [timetextonly] startpage=2 [endpage=24] startpagenumber=1" 

  where almost all above -commmand options can used the same, except the same logical position in side of command!
  where | character means logical or. And it can't be part of command.

  where startpagenumber means that with that *page number* are starting from numbering pages.
  where startpage means that with that *page* are starting from numbering pages.
  where endpage means that with after that *page* and later are stopped from numbering pages.
  where center means the center marginal position to printed.
  where left means the left marginal position to printed.
  where right means the right marginal position to printed.
  where printtime means print time to printed value in the form dd.mm.yyyy hh:mm
  where printtext means string value to printed.
  where pagenumbers means string value to printed.
  where printtime_text means string value to printed with printtime_left etc.
  where timetextonly means string value is only text, which will printed with printtime_left etc.
  and where values and variables for -command are optional => [].
