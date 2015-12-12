# Set terminal & output
set terminal postscript eps enhanced color solid font 'Helvetica,25'

# Output
set output outputfile

# Axis
#set yrange [0:400]
set ylabel "Throughput (x1000 txn/s)"
set xrange [0:50]
set xlabel "Clients"
set xtics nomirror rotate by -30 scale 0.5
set xtics font "Helvetica,16"

# Key
#set key default
#set key box
#set key inside vert
#set key bottom right
set key top left

# Plot type
set style data linespoints

# define grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid back ls 12

# Lines
#set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 2 pointtype 1 pointsize 1.5   # --- blue
set style line 1 lc rgb '#00ad1a' lt 1 lw 2 pt 9 ps 2 # green
set style line 2 lc rgb '#0060ad' lt 1 lw 2 pt 2 ps 2
set style line 3 lc rgb '#dd181f' lt 1 lw 2 pt 3 ps 2
set style line 4 lc rgb '#dd181f' lt 1 lw 2 pt 4 ps 2
set style line 5 lc rgb '#0060ad' lt 1 lw 2 pt 5 ps 2
set style line 6 lc rgb '#82CA4A' lt 1 lw 2 pt 6 ps 2

set datafile separator ','


#every A:B:C:D:E:F
#A: line increment
#B: data block increment
#C: The first line
#D: The first data block
#E: The last line
#F: The last data block


plot data3 every 1::::4 using 1:($3/1000) with linespoints ls 1 title 'Linear Scalability', data1 every 6::::25 using 1:($3/1000) with linespoints ls 5 title 'Weak-DB', data2 every 6::::25 using 1:($3/1000) with linespoints ls 3 title 'Galera'




