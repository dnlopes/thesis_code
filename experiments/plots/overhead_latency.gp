# Set terminal & output
set terminal postscript eps enhanced color solid font 'Helvetica,25'
#set xtics ("6" 0.25, "2" 1.75, "24" 3.25, "48" 4)

# Output
set output outputfile
set datafile separator ','

# Axis
#set yr [0:10]
set ylabel "Latency (ms)"
#set xrange [0:100000]
set xlabel "Clients"
#set xtics nomirror rotate by -30 scale 0.5
#set xtics font "Helvetica,16"

# Key
#set key default
#set key box
#set key inside vert
#set key bottom right
set key top left

# Plot type
set style data histogram
set style histogram cluster gap 1
set style fill solid border rgb "black"
set xtic scale 0.5
set auto x
set yrange [0:*]

# define grid
#set style line 12 lc rgb '#808080' lt 0 lw 1
#set grid back ls 12

# Lines
#set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 2 pointtype 1 pointsize 1.5   # --- blue
set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 1 ps 2
set style line 2 lc rgb '#0060ad' lt 1 lw 2 pt 2 ps 2
set style line 3 lc rgb '#dd181f' lt 1 lw 2 pt 3 ps 2
set style line 4 lc rgb '#dd181f' lt 1 lw 2 pt 4 ps 2
set style line 5 lc rgb '#0060ad' lt 1 lw 2 pt 5 ps 2
set style line 6 lc rgb '#82CA4A' lt 1 lw 2 pt 6 ps 2

plot data1 every 24::4 using 7:xtic(1) with histogram ls 5 title 'Unique IDs', data2 every 24::4 using 7:xtic(1) with histogram ls 3 title 'Unique+Seq IDs'






