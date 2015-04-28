/*global module:false*/
module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    // Metadata.
    pkg: grunt.file.readJSON('package.json'),
    banner: '/*! <%= pkg.title || pkg.name %> - v<%= pkg.version %> - ' +
      '<%= grunt.template.today("yyyy-mm-dd") %>\n' +
      '<%= pkg.homepage ? "* " + pkg.homepage + "\\n" : "" %>' +
      '* Copyright (c) <%= grunt.template.today("yyyy") %> <%= pkg.author.name %>;' +
      ' Licensed <%= _.pluck(pkg.licenses, "type").join(", ") %> */\n',
    // Task configuration.
    clean: {
      all: ['build', 'dist'],
      build: ['build']
    },
    concat: {
      options: {
        banner: '<%= banner %>',
        stripBanners: true
      },
      build: {
        src: ['lib/*.js'],
        dest: 'build/lib/<%= pkg.name %>.js'
      }
    },
    uglify: {
      options: {
        banner: '<%= banner %>'
      },
      dist: {
        src: '<%= concat.build.dest %>',
        dest: 'dist/lib/<%= pkg.name %>.min.js'
      }
    },
    jshint: {
      options: {
        curly: true,
        eqeqeq: true,
        immed: true,
        latedef: true,
        newcap: true,
        noarg: true,
        sub: true,
        undef: true,
        unused: true,
        boss: true,
        eqnull: true,
        browser: true,
        globals: {}
      },
      gruntfile: {
        src: 'Gruntfile.js'
      },
      lib_test: {
        src: ['lib/**/*.js', 'test/**/*.js']
      }
    },
    qunit: {
      files: ['test/**/*.html']
    },
    watch: {
      gruntfile: {
        files: '<%= jshint.gruntfile.src %>',
        tasks: ['jshint:gruntfile']
      },
      lib_test: {
        files: '<%= jshint.lib_test.src %>',
        tasks: ['jshint:lib_test', 'qunit']
      }
    },
    less: {
      build: {
        options: {
          path: ['css'],
          cleancss: true
        },
        files: {
          'build/css/<%= pkg.name %>.css': 'css/<%= pkg.name %>.less'
        }
      }
    },
    copy: {
      main: {
        files: [
          {expand: true, src: ['css/*.css'], dest: 'build/', filter: 'isFile'},
          {expand: true, src: ['img/ap_icons_white_social.png', 'favicon.ico', 'robots.txt'], dest: 'dist/', filter: 'isFile'}
        ]
      },
      modify: {
        src: 'build/index.html',
        dest: 'dist/index.html',
        options: {
          //process: function (content, srcpath) {
          //  return content.replace(/[sad ]/g,"_");
          //}
        }
      }
    },
    cssmin: {
      minify: {
        expand: true,
        cwd: 'build/css/',
        src: ['*.css', '!*.min.css'],
        dest: 'dist/css/',
        ext: '.min.css'
      }
    },
    htmlmin: {
      dist: {
        options: {
          removeComments: true,
          collapseWhitespace: true
        },
        files: {
          'build/index.html': 'index.html',
          'dist/partials/home.html': 'partials/home.html',
          'dist/partials/404.html': 'partials/404.html'
        }
      }
    },
    imagemin: {
      files: { }
    },
    svgmin: {
      options: {
        plugins: [
          {removeViewBox: false},
          {removeUselessStrokeAndFill: false}
        ]
      },
      dist: {
        files: {
          'dist/img/lvl-logo.svg': 'img/lvl-logo.svg'
        }
      }
    }
  });

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-qunit');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-imagemin');
  grunt.loadNpmTasks('grunt-contrib-htmlmin');
  grunt.loadNpmTasks('grunt-svgmin');

  // Default task.
  grunt.registerTask('default', ['clean:all', 'concat', 'uglify', 'less', 'copy:main', 'cssmin', 'htmlmin', 'imagemin', 'svgmin', 'copy:modify', 'clean:build']);

  // Test task
  grunt.registerTask('test', ['jshint', 'qunit']);

};
